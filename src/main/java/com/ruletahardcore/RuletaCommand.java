package com.ruletahardcore;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

/** /ruletahardcore <color 0-7> <titulo>|<texto>  (solo admins, nivel 2). El menu del item llama a este comando. */
public class RuletaCommand {
    public static final String KEY = "ruletahardcore.spin";

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("ruletahardcore")
            .requires(s -> s.hasPermission(2))
            .then(Commands.argument("color", IntegerArgumentType.integer(0, 7))
                .then(Commands.argument("datos", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        int idx = IntegerArgumentType.getInteger(ctx, "color");
                        String datos = StringArgumentType.getString(ctx, "datos");
                        String titulo = "";
                        String texto = datos;
                        int p = datos.indexOf('|');
                        if (p >= 0) {
                            titulo = datos.substring(0, p).trim();
                            texto = datos.substring(p + 1).trim();
                        }
                        // Sin nombre de quien giro: el mensaje no incluye al jugador.
                        ctx.getSource().getServer().getPlayerList()
                            .broadcastSystemMessage(build(idx, titulo, texto), false);
                        return 1;
                    }))));
    }

    public static Component build(int idx, String titulo, String texto) {
        Style color = Style.EMPTY.withColor(TextColor.fromRgb(RuletaColor.of(idx).rgb));
        Style linea = Style.EMPTY.withColor(ChatFormatting.RED).withStrikethrough(true);
        String espacios = " ".repeat(48);
        MutableComponent msg = Component.empty()
            .append(Component.literal(espacios).withStyle(linea))
            .append("\n");
        if (!titulo.isEmpty()) {
            msg.append(Component.literal(titulo.toUpperCase(Locale.ROOT)).withStyle(color.withBold(true)))
               .append("\n");
        }
        msg.append(Component.literal(texto).withStyle(color))
           .append("\n")
           .append(Component.literal(espacios).withStyle(linea));
        // arg 0 = mensaje visible, arg 1 = color (el cliente lo usa para lanzar la animacion)
        return Component.translatable(KEY, msg, idx);
    }
}
