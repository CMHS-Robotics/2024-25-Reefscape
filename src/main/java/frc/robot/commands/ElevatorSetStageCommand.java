package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.Elevator;

public class ElevatorSetStageCommand extends Command {
   Elevator Elevator;

   int level;
  
   public ElevatorSetStageCommand(Elevator e, int l){
      Elevator = e;
      level = l;
      
      addRequirements(Elevator);  
   }

    @Override
    public void initialize(){      
      Elevator.setTargetPosition(level);
      Elevator.setStageLevel(level);

    }

    @Override
    public void execute(){
    }
    
    public boolean end(){
      return Elevator.hasReachedTarget();
    }

    @Override
    public boolean isFinished() {
       return end();
    }
   }