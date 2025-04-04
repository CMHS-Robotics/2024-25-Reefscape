package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.RobotContainer;
import frc.robot.subsystems.DriveAugments;

public class ChangeSpeedMultiplierCommand extends Command {

    DriveAugments Augments;
    double wheels;
    double turn;
    double Speed;
    double rotSpeed;

    public ChangeSpeedMultiplierCommand(DriveAugments d,double w, double t){
        Augments = d;
        wheels = w;
        turn = t;

        addRequirements(d);
   }

   public ChangeSpeedMultiplierCommand(DriveAugments d){
    Augments = d;
    addRequirements(d);
    }

    @Override
    public void execute(){
        RobotContainer.SpeedMultiplier = wheels;
        RobotContainer.RotationSpeedMultiplier = turn;
    }
    
    public Command run(double speed,double rotspeed){
        Speed = speed;
        rotSpeed = rotspeed;
        return Commands.run(()->{
        RobotContainer.SpeedMultiplier = speed;
        RobotContainer.RotationSpeedMultiplier = rotspeed;
        },Augments);
    }

    @Override
    public void initialize(){
    }

    
    public double getTransMult(){
        return Speed;
    }

    public double getRotMult(){
        return rotSpeed;
    }

    @Override
    public boolean isFinished() {
       return false;
    }

}
