// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix6.swerve.SwerveModule.DriveRequestType;
import com.ctre.phoenix6.swerve.SwerveRequest;
import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.events.EventTrigger;
import com.pathplanner.lib.path.PathConstraints;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.util.Units;
import static edu.wpi.first.units.Units.MetersPerSecond;
import static edu.wpi.first.units.Units.RadiansPerSecond;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Direction;
import frc.robot.commands.CoralSetSpinSpeedCommandV2;
import frc.robot.commands.CoralWristSetTargetPositionCommand;
import frc.robot.commands.ElevatorSetStageCommand;
import frc.robot.commands.LockOnAprilTagRotationCommand;
import frc.robot.commands.ZeroTalonCommand;
import frc.robot.generated.TunerConstants;
import frc.robot.subsystems.CommandSwerveDrivetrain;
import frc.robot.subsystems.CoralSpinV2;
import frc.robot.subsystems.CoralWristV2;
import frc.robot.subsystems.DriveAugments;
import frc.robot.subsystems.Elevator;
import frc.robot.subsystems.Vision;
import frc.robot.tools.DashboardSuite;
;

public class RobotContainer {   


    

    private final SendableChooser<Command> autoChooser;
    public static double SpeedMultiplier = 1;
    public static double RotationSpeedMultiplier = 1;
    public static double MaxSpeed = TunerConstants.kSpeedAt12Volts.in(MetersPerSecond); // kSpeedAt12Volts desired top speed
    public static double MaxAngularRate = RotationsPerSecond.of(0.75).in(RadiansPerSecond); // 3/4 of a rotation per second max angular velocity


    /* Setting up bindings for necessary control of the swerve drive platform */
    private final SwerveRequest.FieldCentric drive = new SwerveRequest.FieldCentric()
            .withDeadband(MaxSpeed * 0.1).withRotationalDeadband(MaxAngularRate * 0.05) // Add a 10% deadband
            .withDriveRequestType(DriveRequestType.OpenLoopVoltage); // Use open-loop control for drive motors
    private final SwerveRequest.SwerveDriveBrake brake = new SwerveRequest.SwerveDriveBrake();
    private final SwerveRequest.PointWheelsAt point = new SwerveRequest.PointWheelsAt();


    private final Telemetry logger = new Telemetry(MaxSpeed);
    public static CommandXboxController Driver = new CommandXboxController(0);
    public static CommandXboxController Manipulator = new CommandXboxController(1);
    //subsystems
    CoralSpinV2 CoralSpin = new CoralSpinV2(Manipulator);
    CoralWristV2 CoralWrist = new CoralWristV2(Manipulator);
    Elevator Elevator = new Elevator(Manipulator,CoralWrist);
    DriveAugments Augment = new DriveAugments(Driver,Elevator);
    //AlgaeIntake AlgaeIntake = new AlgaeIntake(Manipulator);

    //commands
    ElevatorSetStageCommand TopStage = new ElevatorSetStageCommand(Elevator,4);
    ElevatorSetStageCommand IntakeStage = new ElevatorSetStageCommand(Elevator,1);
    ElevatorSetStageCommand BottomStage = new ElevatorSetStageCommand(Elevator,0);
    CoralWristSetTargetPositionCommand TopCoral = new CoralWristSetTargetPositionCommand(CoralWrist,-5.78);
    CoralWristSetTargetPositionCommand IntakeCoral = new CoralWristSetTargetPositionCommand(CoralWrist,1);
    CoralWristSetTargetPositionCommand BottomCoral = new CoralWristSetTargetPositionCommand(CoralWrist,0);


    CoralSetSpinSpeedCommandV2 CoralIn = new CoralSetSpinSpeedCommandV2(CoralSpin,-0.3);
    CoralSetSpinSpeedCommandV2 CoralOut = new CoralSetSpinSpeedCommandV2(CoralSpin,0.3);


    
    public final CommandSwerveDrivetrain drivetrain = TunerConstants.createDrivetrain();
    
    //shuffleboard
    Vision Vision = new Vision(drivetrain);
    DashboardSuite Dashboard = new DashboardSuite(Elevator, CoralSpin, CoralWrist, Vision);



    Pose2d targetPose = new Pose2d(7.568, 7.62, Rotation2d.fromDegrees(0));

    // Create the constraints to use while pathfinding
    PathConstraints constraints = new PathConstraints(
            3.0, 1.0,
            Units.degreesToRadians(540), Units.degreesToRadians(720));
    
    // Since AutoBuilder is configured, we can use it to build pathfinding commands
    Command pathfindingCommand = AutoBuilder.pathfindToPose(
            targetPose,
            constraints,
            0.0 // Goal end velocity in meters/sec
    );

    public RobotContainer() {
        CameraServer.startAutomaticCapture();

        Elevator.ElevatorRight.setPosition(0);
        Elevator.ElevatorLeft.setPosition(0);

        //auto commands and events
        NamedCommands.registerCommand("TopStage", TopStage.andThen(TopCoral));
        NamedCommands.registerCommand("IntakeStage", IntakeStage.alongWith(IntakeCoral));
        NamedCommands.registerCommand("BottomStage", BottomStage.alongWith(BottomCoral));
        NamedCommands.registerCommand("CoralIn", CoralIn.withTimeout(3));
        NamedCommands.registerCommand("CoralOut", CoralOut.withTimeout(1));

        new EventTrigger("Elevator").onTrue(Commands.print("Running Elevator"));

        autoChooser = AutoBuilder.buildAutoChooser();

        SmartDashboard.updateValues();
        SmartDashboard.putData("Auto Mode", autoChooser);

        configureBindings();

        //controls


        /*
          Driver:

            d pad gives precise orthagonal and diagonal control
            left stick is drive
            right stick is turn
            left bumper is reset field orientation
            b is brake 
            left trigger is slow
            right trigger is super slow          
         
         */


        /*Manipulator
        

        D-pad: elevator levels
        down-intake stage
        left-stage 1
        up-stage 2
        right-stage 3

        b brings it back to the bottom



        hold right trigger to engage manual elevator control with right stick
        will update the pid target position, so after disengaging control, elevator will stay


        hold left trigger to engage manual wrist control with left stick
        will currently NOT update target pid, so after disengaging manual control, will snap back to the posiition correlating to the elevator level

        click in right stick to reset elevator encoder to 0

        click in left stick to reset wrist encoder to 0




        */
    }


    private void configureBindings() {
        // Note that X is defined as forward according to WPILib convention,
        // and Y is defined as to the left according to WPILib convention.
        drivetrain.setDefaultCommand(
            // Drivetrain will execute this command periodically
            drivetrain.applyRequest(() ->
                drive.withVelocityX(-Driver.getLeftY() * MaxSpeed * SpeedMultiplier) // Drive forward with negative Y (forward)
                    .withVelocityY(-Driver.getLeftX() * MaxSpeed * SpeedMultiplier  ) // Drive left with negative X (left)
                    .withRotationalRate(-Driver.getRightX() * MaxAngularRate * RotationSpeedMultiplier  ) // Drive counterclockwise with negative X (left)
            )
            
        );
        //d pad precise positioning
        Driver.povDown().whileTrue(drivetrain.applyRequest(() -> drive.withVelocityX(-1 * MaxSpeed * SpeedMultiplier  )));
        Driver.povUp().whileTrue(drivetrain.applyRequest(() -> drive.withVelocityX(1 * MaxSpeed * SpeedMultiplier  )));
        Driver.povLeft().whileTrue(drivetrain.applyRequest(() -> drive.withVelocityY(1 * MaxSpeed * SpeedMultiplier  )));
        Driver.povRight().whileTrue(drivetrain.applyRequest(() -> drive.withVelocityY(-1 * MaxSpeed * SpeedMultiplier  )));

        Driver.povDownLeft().whileTrue(drivetrain.applyRequest(() -> drive.withVelocityX(-1/Math.sqrt(2) * MaxSpeed * SpeedMultiplier  ).withVelocityY(1/Math.sqrt(2)*MaxSpeed*SpeedMultiplier  )));
        Driver.povUpRight().whileTrue(drivetrain.applyRequest(() -> drive.withVelocityX(1/Math.sqrt(2) * MaxSpeed * SpeedMultiplier  ).withVelocityY(-1/Math.sqrt(2) * MaxSpeed * SpeedMultiplier  )));
        Driver.povUpLeft().whileTrue(drivetrain.applyRequest(() -> drive.withVelocityX(1/Math.sqrt(2) * MaxSpeed * SpeedMultiplier  ).withVelocityY(1/Math.sqrt(2) * MaxSpeed * SpeedMultiplier  )));
        Driver.povDownRight().whileTrue(drivetrain.applyRequest(() -> drive.withVelocityX(-1/Math.sqrt(2) * MaxSpeed * SpeedMultiplier  ).withVelocityY(-1/Math.sqrt(2) * MaxSpeed * SpeedMultiplier  )));

        Driver.b().whileTrue(drivetrain.applyRequest(() -> brake));
        Driver.y().whileTrue(drivetrain.applyRequest(() ->
            point.withModuleDirection(new Rotation2d(-Driver.getLeftY(), -Driver.getLeftX()))
        ));


        // Run SysId routines when holding back/start and X/Y.
        // Note that each routine should be run exactly once in a single log.
        Driver.back().and(Driver.y()).whileTrue(drivetrain.sysIdDynamic(Direction.kForward));
        Driver.back().and(Driver.x()).whileTrue(drivetrain.sysIdDynamic(Direction.kReverse));
        Driver.start().and(Driver.y()).whileTrue(drivetrain.sysIdQuasistatic(Direction.kForward));
        Driver.start().and(Driver.x()).whileTrue(drivetrain.sysIdQuasistatic(Direction.kReverse));

        // reset the field-centric heading on left bumper press
        Driver.leftBumper().onTrue(drivetrain.runOnce(() -> drivetrain.seedFieldCentric()));
        
        //Driver.back().onTrue(pathfindingCommand);

        Driver.rightBumper().onTrue(new LockOnAprilTagRotationCommand(drivetrain, Vision, Driver));
        
        drivetrain.registerTelemetry(logger::telemeterize);
    }

    
    public void zeroMotors(){
        new ZeroTalonCommand(Elevator.ElevatorLeft).alongWith(new ZeroTalonCommand(Elevator.ElevatorRight)).alongWith(new ZeroTalonCommand(CoralWrist.CoralWrist)).schedule();
    }


    public Command getAutonomousCommand() {
        return autoChooser.getSelected();
    }
}
