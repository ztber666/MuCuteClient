package com.mucheng.mucute.client.game.module.misc

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.data.Ability
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket

class NoClipModule : Module("no_clip", ModuleCategory.Misc) {

    private val enableNoClipAbilitiesPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries.toTypedArray())
            abilityValues.addAll(
                arrayOf(
                    Ability.BUILD,
                    Ability.MINE,
                    Ability.DOORS_AND_SWITCHES,
                    Ability.OPEN_CONTAINERS,
                    Ability.ATTACK_PLAYERS,
                    Ability.ATTACK_MOBS,
                    Ability.MAY_FLY,
                    Ability.FLY_SPEED,
                    Ability.WALK_SPEED,
                    Ability.OPERATOR_COMMANDS
                )
            )
            abilityValues.add(Ability.NO_CLIP)
            walkSpeed = 0.1f
            flySpeed = 0.15f
        })
    }

    private val disableNoClipAbilitiesPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries.toTypedArray())
            abilityValues.addAll(
                arrayOf(
                    Ability.BUILD,
                    Ability.MINE,
                    Ability.DOORS_AND_SWITCHES,
                    Ability.OPEN_CONTAINERS,
                    Ability.ATTACK_PLAYERS,
                    Ability.ATTACK_MOBS,
                    Ability.FLY_SPEED,
                    Ability.WALK_SPEED,
                    Ability.OPERATOR_COMMANDS
                )
            )
            abilityValues.remove(Ability.NO_CLIP)
            walkSpeed = 0.1f
        })
    }

    private var noClipEnabled = false
    private var lastNoClipPacketTime = System.currentTimeMillis()

    override fun onEnabled() {
        if (isSessionCreated) {
            sendToggleMessage(true)
        }
    }

    override fun onDisabled() {
        if (isSessionCreated) {
            sendToggleMessage(false)
        }
    }

    private fun sendToggleMessage(enabled: Boolean) {
        val status = if (enabled) "§aEnabled" else "§cDisabled"
        val message = "§l§b[MuCute] §r§7NoClip §8» $status"

        val textPacket = TextPacket().apply {
            type = TextPacket.Type.RAW
            isNeedsTranslation = false
            this.message = message
            xuid = ""
            sourceName = ""
        }

        session.clientBound(textPacket)
    }

    override fun beforePacketBound(packet: BedrockPacket): Boolean {
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastNoClipPacketTime < 200) {
                return false
            }

            if (!noClipEnabled && isEnabled) {
                enableNoClipAbilitiesPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
                session.clientBound(enableNoClipAbilitiesPacket)
                noClipEnabled = true
            } else if (noClipEnabled && !isEnabled) {
                disableNoClipAbilitiesPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
                session.clientBound(disableNoClipAbilitiesPacket)
                noClipEnabled = false
            }

            lastNoClipPacketTime = currentTime
        }
        return false
    }
}