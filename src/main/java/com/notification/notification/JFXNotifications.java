package com.notification.notification;


import java.lang.ref.WeakReference;
import java.util.*;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;


public class JFXNotifications {

    /***************************************************************************
     * * Private fields * *
     **************************************************************************/

    private String title;
    private String text;
    private String typeMessage;
    private Node graphic;
    //    private ObservableList<Action> actions = FXCollections.observableArrayList();
    private Pos position = Pos.BOTTOM_RIGHT;
    private Duration hideAfterDuration = Duration.seconds(5);
    private boolean hideCloseButton;
    private EventHandler<ActionEvent> onAction;
    private Window owner;
    private Screen screen = Screen.getPrimary();

    private List<String> styleClass = new ArrayList<>();

    /***************************************************************************
     * * Constructors * *
     **************************************************************************/

    // we do not allow instantiation of the Notifications class directly - users
    // must go via the builder API (that is, calling create())
    private JFXNotifications() {
        // no-op
    }

    /***************************************************************************
     * * Public API * *
     **************************************************************************/

    /**
     * Call this to begin the process of building a notification to show.
     */
    public static JFXNotifications create() {
        return new JFXNotifications();
    }

    /**
     * Specify the text to show in the notification.
     */
    public JFXNotifications text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Specify the title to show in the notification.
     */
    public JFXNotifications title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Specify the graphic to show in the notification.
     */
    public JFXNotifications graphic(Node graphic) {
        this.graphic = graphic;
        return this;
    }
    /**
     * Specify the position of the notification on screen, by default it is
     * {@link Pos#BOTTOM_RIGHT bottom-right}.
     */
    public JFXNotifications position(Pos position) {
        this.position = position;
        return this;
    }

    /**
     * The dialog window owner - which can be {@link Screen}, {@link Window}
     * or {@link Node}. If specified, the notifications will be inside
     * the owner, otherwise the notifications will be shown within the whole
     * primary (default) screen.
     */
    public JFXNotifications owner(Object owner) {
        if (owner instanceof Screen) {
            this.screen = (Screen) owner;
        } else {
            this.owner = getWindow(owner);
        }
        return this;
    }

    /**
     * Specify the duration that the notification should show, after which it
     * will be hidden.
     */
    public JFXNotifications hideAfter(Duration duration) {
        this.hideAfterDuration = duration;
        return this;
    }

    /**
     * Specify what to do when the user clicks on the notification (in addition
     * to the notification hiding, which happens whenever the notification is
     * clicked on).
     */
    public JFXNotifications onAction(EventHandler<ActionEvent> onAction) {
        this.onAction = onAction;
        return this;
    }

    /**
     * Specify that the close button in the top-right corner of the notification
     * should not be shown.
     */
    public JFXNotifications hideCloseButton() {
        this.hideCloseButton = true;
        return this;
    }

    /**
     * Instructs the notification to be shown, and that it should use the
     * built-in 'warning' graphic.
     */
    public void showMessage() {
        graphic(new ImageView(JFXNotifications.class.getResource("/img/dialog-message.png").toExternalForm())); //$NON-NLS-1$
        typeMessage = "MESSAGE";
        show();
    }

    /**
     * Instructs the notification to be shown, and that it should use the
     * built-in 'information' graphic.
     */
    public void showSuccess() {
        graphic(new ImageView(JFXNotifications.class.getResource("/img/dialog-success.png").toExternalForm())); //$NON-NLS-1$
        typeMessage = "SUCCESS";
        show();
    }

    /**
     * Instructs the notification to be shown, and that it should use the
     * built-in 'error' graphic.
     */
    public void showError() {
        graphic(new ImageView(JFXNotifications.class.getResource("/img/dialog-error.png").toExternalForm())); //$NON-NLS-1$
        typeMessage = "ERROR";
        show();
    }

    /**
     * Instructs the notification to be shown, and that it should use the
     * built-in 'confirm' graphic.
     */
    public void showWarning() {
        graphic(new ImageView(JFXNotifications.class.getResource("/img/dialog-warning.png").toExternalForm())); //$NON-NLS-1$
        typeMessage = "WARNING";
        show();
    }

    /**
     * Instructs the notification to be shown.
     */
    private void show() {
        JFXNotifications.NotificationPopupHandler.getInstance().show(this);
    }

    /***************************************************************************
     * * Private support classes * *
     **************************************************************************/

    // not public so no need for JavaDoc
    private static final class NotificationPopupHandler {

        private static final JFXNotifications.NotificationPopupHandler INSTANCE = new JFXNotifications.NotificationPopupHandler();

        private double startX;
        private double startY;
        private double screenWidth;
        private double screenHeight;

        static final JFXNotifications.NotificationPopupHandler getInstance() {
            return INSTANCE;
        }

        private final Map<Pos, List<Popup>> popupsMap = new HashMap<>();
        private final double padding = 15;

        // for animating in the notifications
        private ParallelTransition parallelTransition = new ParallelTransition();

        private boolean isShowing = false;

        public void show(JFXNotifications notification) {
            Window window;
            if (notification.owner == null) {
                /*
                 * If the owner is not set, we work with the whole screen.
                 */
                Rectangle2D screenBounds = notification.screen.getVisualBounds();
                startX = screenBounds.getMinX();
                startY = screenBounds.getMinY();
                screenWidth = screenBounds.getWidth();
                screenHeight = screenBounds.getHeight();

                window = getWindow(null);
            } else {
                /*
                 * If the owner is set, we will make the notifications popup
                 * inside its window.
                 */
                startX = notification.owner.getX();
                startY = notification.owner.getY();
                screenWidth = notification.owner.getWidth();
                screenHeight = notification.owner.getHeight();
                window = notification.owner;
            }
            show(window, notification);
        }

        private void show(Window owner, final JFXNotifications notification) {
            // Stylesheets which are added to the scene of a popup aren't
            // considered for styling. For this reason, we need to find the next
            // window in the hierarchy which isn't a popup.
            Window ownerWindow = owner;
            while (ownerWindow instanceof PopupWindow) {
                ownerWindow = ((PopupWindow) ownerWindow).getOwnerWindow();
            }
            // need to install our CSS
            Scene ownerScene = ownerWindow.getScene();
            if (ownerScene != null) {
                String stylesheetUrl = JFXNotifications.class.getResource("/css/notificationpopup.css").toExternalForm(); //$NON-NLS-1$
                if (!ownerScene.getStylesheets().contains(stylesheetUrl)) {
                    // The stylesheet needs to be added at the beginning so that
                    // the styling can be adjusted with custom stylesheets.
                    ownerScene.getStylesheets().add(0, stylesheetUrl);
                }
            }

            final Popup popup = new Popup();
            popup.setAutoFix(false);

            final Pos p = notification.position;

            final JFXNotificationBar notificationBar = new JFXNotificationBar() {
                @Override
                public String getTitle() {
                    return notification.title;
                }

                @Override
                public String getText() {
                    return notification.text;
                }

                @Override
                public String getTypeNotification() {
                    return notification.typeMessage;
                }

                @Override
                public Node getGraphic() {
                    return notification.graphic;
                }

                @Override
                public boolean isShowing() {
                    return isShowing;
                }

                @Override
                protected double computeMinWidth(double height) {
                    String text = getText();
                    Node graphic = getGraphic();
                    if ((text == null || text.isEmpty()) && (graphic != null)) {
                        return graphic.minWidth(height);
                    }
                    return 400;
                }

                @Override
                protected double computeMinHeight(double width) {
                    String text = getText();
                    Node graphic = getGraphic();
                    if ((text == null || text.isEmpty()) && (graphic != null)) {
                        return graphic.minHeight(width);
                    }
                    return 100;
                }

                @Override
                public boolean isShowFromTop() {
                    return JFXNotifications.NotificationPopupHandler.this.isShowFromTop(notification.position);
                }

                @Override
                public void hide() {
                    isShowing = false;

                    // this would slide the notification bar out of view,
                    // but I prefer the fade out below
                    // doHide();

                    // animate out the popup by fading it
                    createHideTimeline(popup, this, p, Duration.ZERO).play();
                }

                @Override
                public boolean isCloseButtonVisible() {
                    return !notification.hideCloseButton;
                }

                @Override
                public double getContainerHeight() {
                    return startY + screenHeight;
                }

                @Override
                public void relocateInParent(double x, double y) {
                    // this allows for us to slide the notification upwards
                    switch (p) {
                        case BOTTOM_LEFT:
                        case BOTTOM_CENTER:
                        case BOTTOM_RIGHT:
                            popup.setAnchorY(y - padding);
                            break;
                        default:
                            // no-op
                            break;
                    }
                }
            };

            notificationBar.getStyleClass().addAll(notification.styleClass);

            notificationBar.setOnMouseClicked(e -> {
                if (notification.onAction != null) {
                    ActionEvent actionEvent = new ActionEvent(notificationBar, notificationBar);
                    notification.onAction.handle(actionEvent);

                    // animate out the popup
                    createHideTimeline(popup, notificationBar, p, Duration.ZERO).play();
                }
            });

            popup.getContent().add(notificationBar);
            popup.show(owner, 0, 0);

            // determine location for the popup
            double anchorX = 0, anchorY = 0;
            final double barWidth = notificationBar.getWidth();
            final double barHeight = notificationBar.getHeight();

            // get anchorX
            switch (p) {
                case TOP_LEFT:
                case CENTER_LEFT:
                case BOTTOM_LEFT:
                    anchorX = padding + startX;
                    break;

                case TOP_CENTER:
                case CENTER:
                case BOTTOM_CENTER:
                    anchorX = startX + (screenWidth / 2.0) - barWidth / 2.0 - padding / 2.0;
                    break;

                default:
                case TOP_RIGHT:
                case CENTER_RIGHT:
                case BOTTOM_RIGHT:
                    anchorX = startX + screenWidth - barWidth - padding;
                    break;
            }

            // get anchorY
            switch (p) {
                case TOP_LEFT:
                case TOP_CENTER:
                case TOP_RIGHT:
                    anchorY = padding + startY;
                    break;

                case CENTER_LEFT:
                case CENTER:
                case CENTER_RIGHT:
                    anchorY = startY + (screenHeight / 2.0) - barHeight / 2.0 - padding / 2.0;
                    break;

                default:
                case BOTTOM_LEFT:
                case BOTTOM_CENTER:
                case BOTTOM_RIGHT:
                    anchorY = startY + screenHeight - barHeight - padding;
                    break;
            }

            popup.setAnchorX(anchorX);
            popup.setAnchorY(anchorY);

            isShowing = true;
            notificationBar.doShow();

            addPopupToMap(p, popup);

            // begin a timeline to get rid of the popup
            Timeline timeline = createHideTimeline(popup, notificationBar, p, notification.hideAfterDuration);
            timeline.play();
        }

        private void hide(Popup popup, Pos p) {
            popup.hide();
            removePopupFromMap(p, popup);
        }

        private Timeline createHideTimeline(final Popup popup, JFXNotificationBar bar, final Pos p, Duration startDelay) {
            KeyValue fadeOutBegin = new KeyValue(bar.opacityProperty(), 1.0);
            KeyValue fadeOutEnd = new KeyValue(bar.opacityProperty(), 0.0);

            KeyFrame kfBegin = new KeyFrame(Duration.ZERO, fadeOutBegin);
            KeyFrame kfEnd = new KeyFrame(Duration.millis(500), fadeOutEnd);

            Timeline timeline = new Timeline(kfBegin, kfEnd);
            timeline.setDelay(startDelay);
            timeline.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    hide(popup, p);
                }
            });

            return timeline;
        }

        private void addPopupToMap(Pos p, Popup popup) {
            List<Popup> popups;
            if (!popupsMap.containsKey(p)) {
                popups = new LinkedList<>();
                popupsMap.put(p, popups);
            } else {
                popups = popupsMap.get(p);
            }

            doAnimation(p, popup);

            // add the popup to the list so it is kept in memory and can be
            // accessed later on
            popups.add(popup);
        }

        private void removePopupFromMap(Pos p, Popup popup) {
            if (popupsMap.containsKey(p)) {
                List<Popup> popups = popupsMap.get(p);
                popups.remove(popup);
            }
        }

        private void doAnimation(Pos p, Popup changedPopup) {
            List<Popup> popups = popupsMap.get(p);
            if (popups == null) {
                return;
            }

            final double newPopupHeight = changedPopup.getContent().get(0).getBoundsInParent().getHeight();

            parallelTransition.stop();
            parallelTransition.getChildren().clear();

            final boolean isShowFromTop = isShowFromTop(p);

            // animate all other popups in the list upwards so that the new one
            // is in the 'new' area.
            // firstly, we need to determine the target positions for all popups
            double sum = 0;
            double targetAnchors[] = new double[popups.size()];
            for (int i = popups.size() - 1; i >= 0; i--) {
                Popup _popup = popups.get(i);

                final double popupHeight = _popup.getContent().get(0).getBoundsInParent().getHeight();

                if (isShowFromTop) {
                    if (i == popups.size() - 1) {
                        sum = startY + newPopupHeight + padding;
                    } else {
                        sum += popupHeight;
                    }
                    targetAnchors[i] = sum;
                } else {
                    if (i == popups.size() - 1) {
                        sum = changedPopup.getAnchorY() - popupHeight;
                    } else {
                        sum -= popupHeight;
                    }

                    targetAnchors[i] = sum;
                }
            }

            // then we set up animations for each popup to animate towards the
            // target
            for (int i = popups.size() - 1; i >= 0; i--) {
                final Popup _popup = popups.get(i);
                final double anchorYTarget = targetAnchors[i];
                if (anchorYTarget < 0) {
                    _popup.hide();
                }
                final double oldAnchorY = _popup.getAnchorY();
                final double distance = anchorYTarget - oldAnchorY;

                Transition t = new JFXNotifications.NotificationPopupHandler.CustomTransition(_popup, oldAnchorY, distance);
                t.setCycleCount(1);
                parallelTransition.getChildren().add(t);
            }
            parallelTransition.play();
        }

        private boolean isShowFromTop(Pos p) {
            switch (p) {
                case TOP_LEFT:
                case TOP_CENTER:
                case TOP_RIGHT:
                    return true;
                default:
                    return false;
            }
        }

        class CustomTransition extends Transition {

            private WeakReference<Popup> popupWeakReference;
            private double oldAnchorY;
            private double distance;

            CustomTransition(Popup popup, double oldAnchorY, double distance) {
                popupWeakReference = new WeakReference<>(popup);
                this.oldAnchorY = oldAnchorY;
                this.distance = distance;
                setCycleDuration(Duration.millis(350.0));
            }

            @Override
            protected void interpolate(double frac) {
                Popup popup = popupWeakReference.get();
                if (popup != null) {
                    double newAnchorY = oldAnchorY + distance * frac;
                    popup.setAnchorY(newAnchorY);
                }
            }

        }
    }

    public static Window getWindow(Object owner) throws IllegalArgumentException {
        if (owner == null) {
            Window window = null;
            // lets just get the focused stage and show the dialog in there
            @SuppressWarnings("deprecation")
            Iterator<Window> windows = Window.impl_getWindows();
            while (windows.hasNext()) {
                window = windows.next();
                if (window.isFocused() && !(window instanceof PopupWindow)) {
                    break;
                }
            }
            return window;
        } else if (owner instanceof Window) {
            return (Window) owner;
        } else if (owner instanceof Node) {
            return ((Node) owner).getScene().getWindow();
        } else {
            throw new IllegalArgumentException("Unknown owner: " + owner.getClass()); //$NON-NLS-1$
        }
    }
}

