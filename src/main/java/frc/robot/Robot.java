// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.DutyCycleOut;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;

import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private final CANBus kCANBus = new CANBus("canivore");

  private final TalonFX leftLeader = new TalonFX(1, kCANBus);
  private final TalonFX leftFollower = new TalonFX(2, kCANBus);
  private final TalonFX rightLeader = new TalonFX(3, kCANBus);
  private final TalonFX rightFollower = new TalonFX(4, kCANBus);

  SparkMax shooterLeader;
  SparkMax shooterFollower;

  private final DutyCycleOut leftOut = new DutyCycleOut(0);
  private final DutyCycleOut rightOut = new DutyCycleOut(0);



  private final XboxController joystick = new XboxController(0);

  // private int printCount = 0;

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  public Robot() {
    /* Configure the devices */
    var leftConfiguration = new TalonFXConfiguration();
    var rightConfiguration = new TalonFXConfiguration();

    /* User can optionally change the configs or leave it alone to perform a factory default */
    leftConfiguration.MotorOutput.Inverted = InvertedValue.CounterClockwise_Positive;
    rightConfiguration.MotorOutput.Inverted = InvertedValue.Clockwise_Positive;

    leftLeader.getConfigurator().apply(leftConfiguration);
    leftFollower.getConfigurator().apply(leftConfiguration);
    rightLeader.getConfigurator().apply(rightConfiguration);
    rightFollower.getConfigurator().apply(rightConfiguration);

    /* Set up followers to follow leaders */
    leftFollower.setControl(new Follower(leftLeader.getDeviceID(), false));
    rightFollower.setControl(new Follower(rightLeader.getDeviceID(), false));
  
    leftLeader.setSafetyEnabled(true);
    rightLeader.setSafetyEnabled(true);

    shooterLeader = new SparkMax(1, MotorType.kBrushless);
    shooterFollower = new SparkMax(1, MotorType.kBrushless);


    SparkMaxConfig globalConfig = new SparkMaxConfig();
    SparkMaxConfig shooterLeaderConfig = new SparkMaxConfig();
    SparkMaxConfig shooterFollowerConfig = new SparkMaxConfig();

    globalConfig
      .smartCurrentLimit(50)
      .idleMode(IdleMode.kCoast);

    shooterLeaderConfig
      .apply(globalConfig);
    
    shooterFollowerConfig
       .apply(globalConfig)
       .follow(shooterLeader.getDeviceId());

    shooterLeader.configure(shooterLeaderConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    shooterFollower.configure(shooterFollowerConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

  }

  @Override
  public void robotPeriodic() {
    // Comented bellow cuz not tryna fill up logs

    // if (++printCount >= 10) {
    //   printCount = 0;
    //   System.out.println("Left out: " + leftLeader.get());
    //   System.out.println("Right out: " + rightLeader.get());
    //   System.out.println("Left Pos: " + leftLeader.getPosition());
    //   System.out.println("Right Pos: " + rightLeader.getPosition());
    // }

    SmartDashboard.putNumber("Left Out", shooterLeader.getAppliedOutput());
    SmartDashboard.putNumber("Right Out", shooterFollower.getAppliedOutput());
  }

  @Override
  public void autonomousInit() {}

  @Override
  public void autonomousPeriodic() {}

  @Override
  public void teleopInit() {}

  @Override
  public void teleopPeriodic() {
    /* Get forward and rotational throttle from joystick */
    /* invert the joystick Y because forward Y is negative */
    double fwd = -joystick.getLeftY();
    double rot = joystick.getRightX();
    /* Set output to control frames */
    leftOut.Output = fwd + rot;
    rightOut.Output = fwd - rot;
    /* And set them to the motors */
    if (!joystick.getAButton()) {
      leftLeader.setControl(leftOut);
      rightLeader.setControl(rightOut);
    }

    double shooterPercentOutput = joystick.getRightTriggerAxis();
    shooterLeader.set(-shooterPercentOutput);
  }

  @Override
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() {
    /* Zero out controls so we aren't just relying on the enable frame */
    leftOut.Output = 0;
    rightOut.Output = 0;
    leftLeader.setControl(leftOut);
    rightLeader.setControl(rightOut);
  }

  @Override
  public void testInit() {}

  @Override
  public void testPeriodic() {}

  @Override
  public void simulationInit() {}

  @Override
  public void simulationPeriodic() {}
}
