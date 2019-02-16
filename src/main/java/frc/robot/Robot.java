/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import java.io.File;

import com.revrobotics.CANEncoder;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.subsystems.CameraController;
import frc.robot.subsystems.CargoArm;
import frc.robot.subsystems.Climb;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.HatchArm;
import frc.robot.teleopCommands.TeleopCameraController;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.followers.EncoderFollower;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private static final String kVisionAuto = "VisionFollow";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  
  public static BaseCamera camera = new ImplCamera();
  public static Drivetrain drivetrain = new Drivetrain();
  public static CameraController cameraController = new CameraController();
  public static CargoArm cargoArm = new CargoArm();
  public static Climb climb = new Climb();
  public static HatchArm hatchArm = new HatchArm();

  private CANEncoder leftEncoder = drivetrain.getLeftEncoder();
  private CANEncoder rightEncoder = drivetrain.getRightEncoder();
  private ADXRS450_Gyro gyro = drivetrain.getGyro();

  EncoderFollower leftFollower;
  EncoderFollower rightFollower;

  private NetworkTableInstance inst = NetworkTableInstance.getDefault();
  private NetworkTable visionTable = inst.getTable("TestTable");

  public static OI oi = new OI();

  private Command teleopCameraController = new TeleopCameraController();

  /**
   * This function is run when the robot is first started up and should be used
   * for any initialization code.
   */

  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    // System.out.println("Running robot init");
    File leftFile = new File("/home/lvuser/deploy/ForwardLeftThenRight.left.pf1.csv");
    File rightFile = new File("/home/lvuser/deploy/ForwardLeftThenRight.right.pf1.csv");
    Trajectory leftTraj = Pathfinder.readFromCSV(rightFile);
    Trajectory rightTraj = Pathfinder.readFromCSV(leftFile);
    // for (int i = 0; i < leftTraj.length(); i++) {
    // System.out.println(leftTraj.get(i).velocity);
    // }
    leftFollower = new EncoderFollower(leftTraj);
    rightFollower = new EncoderFollower(rightTraj);

    // encoder position, 360 ticks/revolution, 0.1524 m = 6 in wheel diameter
    // right encoder is different: 250 ticks/revolution
    leftFollower.configureEncoder((int) leftEncoder.getPosition(), 360, 0.1524);
    rightFollower.configureEncoder((int) rightEncoder.getPosition(), 250, 0.1524);

    leftFollower.configurePIDVA(1, 0, 0.9, 1 / 2.5, 0);
    rightFollower.configurePIDVA(1, 0, 0.9, 1 / 3.2, 0);
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for
   * items like diagnostics that you want ran during disabled, autonomous,
   * teleoperated and test.
   *
   * <p>
   * This runs after the mode specific periodic functions, but before LiveWindow
   * and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable chooser
   * code works with the Java SmartDashboard. If you prefer the LabVIEW Dashboard,
   * remove all of the chooser code and uncomment the getString line to get the
   * auto name from the text box below the Gyro
   *
   * <p>
   * You can add additional auto modes by adding additional comparisons to the
   * switch structure below with additional strings. If using the SendableChooser
   * make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    m_autoSelected = kVisionAuto;
    // autoSelected = SmartDashboard.getString("Auto Selector",
    // defaultAuto);
    // System.out.println("Auto selected: " + m_autoSelected);

    switch (m_autoSelected) {
    case kCustomAuto:
      break;
    case kDefaultAuto:
      // System.out.println("Auto init done");
      gyro.reset();
      // in meters
      double maxVelocity = 2.0;
      double maxAccel = 2.0;
      double maxJerk = 60.0;

      // generate trajectory
      /*
       * Waypoint[] points = new Waypoint[] { new Waypoint(-4, -1,
       * Pathfinder.d2r(-45)), // Waypoint @ x=-4, y=-1, exit angle=-45 degrees new
       * Waypoint(-2, -2, 0), // Waypoint @ x=-2, y=-2, exit angle=0 radians new
       * Waypoint(0, 0, 0) // Waypoint @ x=0, y=0, exit angle=0 radians };
       * Trajectory.Config config = new
       * Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC,
       * Trajectory.Config.SAMPLES_HIGH, 0.02, maxVelocity, maxAccel, maxJerk);
       * Trajectory trajectory = Pathfinder.generate(points, config);
       * 
       * 
       * // 0.6m = 23.75 in wheelbase (dist b/w left and right wheel) TankModifier
       * modifier = new TankModifier(trajectory).modify(0.6); leftFollower = new
       * EncoderFollower(modifier.getLeftTrajectory()); rightFollower = new
       * EncoderFollower(modifier.getRightTrajectory());
       */

      break;
    }
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    Scheduler.getInstance().run();
    switch (m_autoSelected) {
    case kCustomAuto:
      // Put custom auto code here
      break;
    case kDefaultAuto:
      // Put default auto code here
      // System.out.println("Auto periodic run");
      double leftOutput = leftFollower.calculate((int) leftEncoder.getPosition());
      double rightOutput = rightFollower.calculate((int) rightEncoder.getPosition());

      // double gyro_heading = gyro.getAngle() % 360;
      // double desired_heading = Pathfinder.r2d(leftFollower.getHeading());

      // double angleDifference = Pathfinder.boundHalfDegrees(desired_heading -
      // gyro_heading);
      // double turn = 0.8 * (-1.0/80.0) * angleDifference;
      double turnTraj = 0;
      drivetrain.tank(leftOutput + turnTraj, rightOutput - turnTraj);
      break;
    case kVisionAuto:
      System.out.println("CenterX:" + visionTable.getEntry("centerX").getDouble(0));
      //double turn = visionTable.getEntry("centerX").getDouble(0) - (320 / 2);
      //drivetrain.arcade(0, turn * 0.005);
      break;
    }
  }

  @Override
  public void teleopInit() {
    super.teleopInit();
    teleopCameraController.start();
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    Scheduler.getInstance().run();
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
