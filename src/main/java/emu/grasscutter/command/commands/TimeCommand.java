package emu.grasscutter.command.commands;

import ch.qos.logback.classic.spi.ILoggingEvent;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.data.binout.AbilityEmbryoEntry;
import emu.grasscutter.data.excels.AvatarData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.server.event.internal.ServerLogEvent;
import emu.grasscutter.server.event.types.ServerEvent;
import emu.grasscutter.server.game.GameSession;
import java.time.OffsetDateTime;
import emu.grasscutter.server.packet.send.PacketChangeGameTimeRsp;

import java.util.List;

@Command(label = "time",usage = "time <set/freeze/get>")
public class TimeCommand implements CommandHandler {

    boolean hasFreeze = false;

    Player sender;
    Scene playerCurrentScene;
    Player player;
    int defaultTimeValue = 480;
    int defaultTimeDay = 480;
    int defaultTimeNight = 1300;


    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {

        this.player = targetPlayer;
        this.sender = sender;

        if (args.size() > 2
                || args.size() == 0) {
            CommandHandler.sendMessage(sender, "usage time <set/freeze/get>");
            return;
        }
        // sender input
        String action = args.get(0);
        this.playerCurrentScene = sender.getScene();
        switch (action) {
            case "set":
                //Argument Checker
                if (args.size() < 2) {CommandHandler.sendMessage(sender, "time <set> <timeValue/day/night>"); return;}
                if(IntParseChecker(args.get(1)) != null) {
                    int value = Integer.parseInt(args.get(1));
                    playerCurrentScene.changeTime(value);
                    defaultTimeValue = value;
                    UpdatePacketChangeTimeRsp(targetPlayer);
                    CommandHandler.sendMessage(sender, "SetTime To " + value + " Scene: " + playerCurrentScene.getId());
                    return;
                } else { TimeSetArgsStringType(args.get(1)); return; }
            case "freeze":
                if (args.size() != 1) { CommandHandler.sendMessage(sender, "time freeze"); }
                if (!hasFreeze) {
                    hasFreeze = true;
                    CommandHandler.sendMessage(sender, "FreezeTime: " + hasFreeze);
                    EventUpdateTimeChecker();
                } else {
                    hasFreeze = false;
                    playerCurrentScene.setFreezeTime(false);
                    CommandHandler.sendMessage(sender, "FreezeTime: " + hasFreeze);
                }
                return;
            case "get":
                CommandHandler.sendMessage(sender, "Time: " + playerCurrentScene.getTime());
                return;
            default:
                CommandHandler.sendMessage(sender, "Input your action");
        }
    }
    private Integer IntParseChecker(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    //if ExceptionNumber is Null "do not crash"
    private void TimeSetArgsStringType(String argsInput){
        switch (argsInput) {
            case "day":
                playerCurrentScene.changeTime(defaultTimeDay);
                UpdatePacketChangeTimeRsp(player);
                CommandHandler.sendMessage(sender, "SetTime To Day" + " Scene: " + playerCurrentScene.getId());
                break;
            case "night":
                playerCurrentScene.changeTime(defaultTimeNight);
                UpdatePacketChangeTimeRsp(player);
                CommandHandler.sendMessage(sender, "SetTime To Night" + " Scene: " + playerCurrentScene.getId());
                break;
        }
    }

    // UpdateFunction for easy Write
    private void UpdatePacketChangeTimeRsp(Player player) {
        playerCurrentScene.getWorld().broadcastPacket(new PacketChangeGameTimeRsp(player));
    }


    //EventUpdate Time
    private void EventUpdateTimeChecker() {
        while (hasFreeze) {
            if (playerCurrentScene.getTime() > (defaultTimeValue+100)) {
                playerCurrentScene.changeTime(defaultTimeValue);
                UpdatePacketChangeTimeRsp(player);
            }
        }
    }
}
