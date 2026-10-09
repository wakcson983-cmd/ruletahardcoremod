package com.ruletahardcore;

import com.ruletahardcore.client.ClientHooks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RuletaItem extends Item {
    public RuletaItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            if (player.hasPermissions(2)) {
                ClientHooks.openMenu();
            } else {
                player.displayClientMessage(Component.literal("Solo un admin puede girar la ruleta."), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
