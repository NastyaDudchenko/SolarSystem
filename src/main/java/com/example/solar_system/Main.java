package com.example.solar_system;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Objects;

public class Main extends Application {

    private static final double WIDTH = 1200, HEIGHT = 800;
    private double anchorX, anchorY;
    private double anchorAngelX = 0;
    private double anchorAngelY = 0;

    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);
    private final PointLight pointLight = new PointLight(Color.RED);
    private final Sphere sun = createSphere(100, "/sun.jpg", "/sun_normal.jpg", 0, 0, 0);
    private final Sphere mercury = createSphere(15, "/mercury.jpg", "/mercury_normal.jpg", 200, 0, 50);
    private final Sphere venus = createSphere(25, "/venus.jpg", "/venus_normal.jpg", 400, 0, 100);
    private final Sphere earth = createSphere(30, "/earth.jpg", "/earth_normal.jpg", 600, 0, 150);
    private final Sphere mars = createSphere(20, "/mars.jpg", "/mars_normal.jpg", 800, 0, 200);
    private final Sphere jupiter = createSphere(85, "/jupiter.jpg", "/jupiter_normal.jpg", 1000, 0, 300);
    private final Sphere saturn = createSphere(50, "/saturn.jpg", "/saturn_normal.jpg", 1400, 0, 400);
    private final Sphere uranus = createSphere(40, "/uranus.jpg", "/uranus_normal.jpg", 1200, 0, 500);
    private final Sphere neptune = createSphere(35, "/neptune.jpg", "/neptune_normal.jpg", 1600, 0, 600);
    private final Sphere moon = createSphere(5, "/moon.jpg", "/moon_normal.jpg", 650, 0, 160);
    private final Cylinder rings = createRings("/saturn_circle.png", 1400, 350);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Camera camera = new PerspectiveCamera(true);
        camera.setNearClip(1);
        camera.setLayoutX(0);
        camera.setLayoutY(0);
        camera.setFarClip(100000);
        camera.translateZProperty().set(-2500);

        Group root = new Group();
        SmartGroup universe = new SmartGroup();

        universe.getChildren().add(new AmbientLight());
        universe.getChildren().addAll(sun, pointLight);
        universe.getChildren().addAll(mercury, venus, earth, moon, mars, jupiter, rings, saturn, uranus, neptune);
        root.getChildren().addAll(universe);
        root.getChildren().add(prepareBackground());

        Scene scene = new Scene(root, WIDTH, HEIGHT, true);
        scene.setCamera(camera);

        InitMouseControl(universe, scene, primaryStage);
        primaryStage.setTitle("Solar System");
        primaryStage.setScene(scene);
        primaryStage.show();

        initAnimation();
    }

    private ImageView prepareBackground() {
        ImageView background = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/milky_way.jpg"))));
        background.setPreserveRatio(true);
        background.getTransforms().add(new Translate(-background.getImage().getHeight(), -background.getImage().getWidth() / 4, 5000));
        return background;
    }

    private Sphere createSphere(double radius, String map, String normal, double x, double y, double z) {
        Sphere sphere = new Sphere(radius);

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(new Image(Objects.requireNonNull(Main.class.getResourceAsStream(map))));
        material.setBumpMap(new Image(Objects.requireNonNull(Main.class.getResourceAsStream(normal))));
        material.setSpecularColor(Color.WHITE);
        material.setDiffuseColor(Color.WHITE);
        sphere.setMaterial(material);
        sphere.setRotationAxis(Rotate.Y_AXIS);
        sphere.setTranslateX(x);
        sphere.setTranslateY(y);
        sphere.setTranslateZ(z);

        return sphere;
    }

    private Cylinder createRings(String texture, double x, double z) {
        Cylinder ring = new Cylinder(100, 1);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(new Image(Objects.requireNonNull(Main.class.getResourceAsStream(texture))));
        ring.getTransforms().add(Transform.rotate(5, 5, 0));
        ring.setMaterial(material);
        ring.setTranslateX(x);
        ring.setTranslateZ(z);
        return ring;
    }

    private void initAnimation() {
        double[] orbitalRadii = {200, 400, 600, 800, 1000, 1200, 1400, 1600, 1800};
        double[] orbitalSpeeds = {0.5, 0.3, 0.2, 0.15, 0.1, 0.08, 0.06, 0.05, 0.04};

        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);

        createOrbitAnimation(mercury, orbitalRadii[0], orbitalSpeeds[0]);
        createOrbitAnimation(venus, orbitalRadii[1], orbitalSpeeds[1]);
        createOrbitAnimation(earth, orbitalRadii[2], orbitalSpeeds[2]);
        createOrbitAnimation(mars, orbitalRadii[3], orbitalSpeeds[3]);
        createOrbitAnimation(jupiter, orbitalRadii[4], orbitalSpeeds[4]);
        createOrbitAnimation(saturn, orbitalRadii[5], orbitalSpeeds[5]);
        createOrbitAnimation(rings, orbitalRadii[5], orbitalSpeeds[5]);
        createOrbitAnimation(uranus, orbitalRadii[6], orbitalSpeeds[6]);
        createOrbitAnimation(neptune, orbitalRadii[7], orbitalSpeeds[7]);

        createRotationAnimation(sun);
        createRotationAnimation(mercury);
        createRotationAnimation(venus);
        createRotationAnimation(earth);
        createRotationAnimation(moon);
        createRotationAnimation(mars);
        createRotationAnimation(jupiter);
        createRotationAnimation(saturn);
        createRotationAnimation(uranus);
        createRotationAnimation(neptune);

        createMoonOrbitAroundEarth();

        pointLight.setRotate(pointLight.getRotate() + 1);

        timeline.play();
    }

    private void createOrbitAnimation(Node planet, double radius, double speed) {
        final double[] angle = {0};
        Timeline orbitTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.016), event -> {
                    angle[0] += Math.toRadians(speed);

                    double newX = radius * Math.cos(angle[0]);
                    double newZ = radius * Math.sin(angle[0]);

                    planet.setTranslateX(newX);
                    planet.setTranslateZ(newZ);
                })
        );
        orbitTimeline.setCycleCount(Timeline.INDEFINITE);
        orbitTimeline.play();
    }

    private void createMoonOrbitAroundEarth() {
        double moonOrbitRadius = 60;
        double moonOrbitSpeed = 1;
        final double[] angle = {0};

        Timeline moonOrbitTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.016), event -> {
                    angle[0] += Math.toRadians(moonOrbitSpeed);
                    double newX = moonOrbitRadius * Math.cos(angle[0]);
                    double newZ = moonOrbitRadius * Math.sin(angle[0]);
                    moon.setTranslateX(earth.getTranslateX() + newX);
                    moon.setTranslateZ(earth.getTranslateZ() + newZ);
                })
        );
        moonOrbitTimeline.setCycleCount(Timeline.INDEFINITE);
        moonOrbitTimeline.play();
    }

    private void createRotationAnimation(Node planet) {
        Timeline rotationTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.016), _ -> {
                    planet.setRotate(planet.getRotate() + 0.5);  // Поворот планеты вокруг своей оси
                })
        );
        rotationTimeline.setCycleCount(Timeline.INDEFINITE);
        rotationTimeline.play();
    }

    private void InitMouseControl(SmartGroup universe, Scene scene, Stage primaryStage) {
        Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
        Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
        universe.getTransforms().addAll(xRotate, yRotate);

        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        scene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngelX = angleX.get();
            anchorAngelY = angleY.get();
        });

        scene.setOnMouseDragged(event -> {
            angleX.set(anchorAngelX - (anchorY - event.getSceneY()));
            angleY.set(anchorAngelY + anchorX - event.getSceneX());
        });

        primaryStage.addEventHandler(ScrollEvent.SCROLL, event -> {
            universe.translateZProperty().set(universe.getTranslateZ() + event.getDeltaY());
        });
    }

    static class SmartGroup extends Group {
        private final Affine transform = new Affine();

        public SmartGroup() {
            this.getTransforms().add(transform);
        }

        void rotateBy(Axis axis, int angle) {
            Rotate rotate = switch (axis) {
                case X -> new Rotate(angle, Rotate.X_AXIS);
                case Y -> new Rotate(angle, Rotate.Y_AXIS);
                case Z -> new Rotate(angle, Rotate.Z_AXIS);
            };

            transform.append(rotate);
        }

        enum Axis {X, Y, Z}
    }
}