package emu.grasscutter.command.commands;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.database.DatabaseHelper;
import emu.grasscutter.game.Account;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.event.Event;
import emu.grasscutter.server.event.player.PlayerJoinEvent;
import emu.grasscutter.server.game.GameServer;
import emu.grasscutter.utils.ConfigContainer;

import java.util.List;

@Command(label = "ban", usage = "ban <add/remove/list> <user/id> reason",targetRequirement = Command.TargetRequirement.NONE)
public class BanCommand implements CommandHandler{
    Account targetBanAccount;
    List<Account> accountBanned;

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        if (sender != null) {return;}
        if (args.size() == 0) {
            CommandHandler.sendMessage(null, "usage: ban <add/remove/list> reason");
            return;
        }
        int targetUid;
        String banAction = args.get(0);
        if(banAction == null) {CommandHandler.sendMessage(null, "usage: ban <add/remove/list> reason"); return;}
        switch (banAction) {
            case "add":
                try {
                    targetUid = Integer.parseInt(args.get(1));
                    targetBanAccount = DatabaseHelper.getAccountByPlayerId(targetUid);
                    if (ArgumentBanChecker()) {
                        Player player = Grasscutter.getGameServer().getPlayerByAccountId(targetBanAccount.getId());
                        if (player != null) {
                            player.getSession().close();
                        }
                        targetBanAccount.setBanned(true);
                        CommandHandler.sendMessage(null, "Ban Player " + targetBanAccount.getUsername() + "  Success");
                    } else {
                        return;
                    }
                    break;
                } catch (IndexOutOfBoundsException index) {
                    CommandHandler.sendMessage(null, "ban add <ID>");
                    return;
                }
            case "remove":
                try {
                    targetUid = Integer.parseInt(args.get(1));
                    targetBanAccount = DatabaseHelper.getAccountByPlayerId(targetUid);
                    if(ArgumentUnBanChecker()) {
                        if (targetBanAccount.getBanned()) {
                            targetBanAccount.setBanned(false);
                            CommandHandler.sendMessage(null, "unban " + targetBanAccount.getUsername() + "  Success");
                        } else {
                            return;
                        }
                    }
                    break;
                }catch (IndexOutOfBoundsException index) {
                    CommandHandler.sendMessage(null, "ban remove <ID>");
                    return;
                }
            case "list":
                    try {
                        for (Account acc : DatabaseHelper.getAllAccount()) {
                            if(acc.getBanned()) {
                                CommandHandler.sendMessage(null, "All Data Player ID " + acc.getId() + " Ban Stats " + acc.getBanned());
                                //accountBanned.add(acc);
                            }
                        }

                        // useless
                        /*int banList = accountBannedUserName.size() + 1;
                        CommandHandler.sendMessage(null, "Player Got Ban " + banList + " Player");
                        for (int i = 0; i < accountBannedUserName.size(); i++) {
                            CommandHandler.sendMessage(null, "" + accountBannedUserName.get(i));
                        } */
                        return;
                    } catch (NullPointerException e) {
                        CommandHandler.sendMessage(null, "Player Banned Is Null");
                        return;
                    }
        }
        if(targetBanAccount != null) {targetBanAccount.save();}
    }

    boolean ArgumentBanChecker() {
        if (targetBanAccount == null) {
            CommandHandler.sendMessage(null, "Player Invalid in database");
            return false;
        }

        if(targetBanAccount.getBanned()) {
            CommandHandler.sendMessage(null, "This Player Already Ban");
            return false;
        }
        return true;
    }
    boolean ArgumentUnBanChecker() {
        if (targetBanAccount == null) {
            CommandHandler.sendMessage(null, "Player Invalid in database");
            return false;
        }

        if(!targetBanAccount.getBanned()) {
            CommandHandler.sendMessage(null, "This Player Not Banned");
            return false;
        }
        return true;
    }

}
