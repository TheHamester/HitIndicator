package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.latesthits.HitIndicatorType;
import com.rosymaple.hitindication.latesthits.HitMarkerType;
import com.rosymaple.hitindication.latesthits.PacketsHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = HitIndication.MODID)
public class HitEvents {
    @SubscribeEvent
    public static void onAttack(LivingDamageEvent event) {
        if(event.getSource().getImmediateSource() instanceof PotionEntity)
            return;

        if(!(event.getSource().getTrueSource() instanceof LivingEntity)
                || event.getSource().getTrueSource().getUniqueID().equals(event.getEntity().getUniqueID())) {

            if(!(event.getEntity() instanceof ServerPlayerEntity))
                return;

            ServerPlayerEntity player = (ServerPlayerEntity)event.getEntityLiving();
            int damagePercent = (int)Math.floor((event.getAmount() / player.getMaxHealth() * 100));

            PacketsHelper.addHitIndicator(player, null, HitIndicatorType.ND_RED, damagePercent, false);
            return;
        }

        if(event.getSource().getTrueSource() instanceof ServerPlayerEntity) {
            if(event.getSource().getImmediateSource() instanceof ArrowEntity)
                PacketsHelper.addHitMarker((ServerPlayerEntity)event.getSource().getTrueSource(), HitMarkerType.CRIT);
        }

        if(!(event.getEntityLiving() instanceof ServerPlayerEntity))
            return;

        ServerPlayerEntity player = (ServerPlayerEntity)event.getEntityLiving();
        LivingEntity source = (LivingEntity)event.getSource().getTrueSource();

        int damagePercent = (int)Math.floor((event.getAmount() / player.getMaxHealth() * 100));

        PacketsHelper.addHitIndicator(player, source, HitIndicatorType.RED, damagePercent, false);
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if(!(event.getPlayer() instanceof ServerPlayerEntity) || !event.isVanillaCritical())
            return;

        ServerPlayerEntity player = (ServerPlayerEntity)event.getPlayer();

        PacketsHelper.addHitMarker(player, HitMarkerType.CRIT);
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if(!(event.getSource().getTrueSource() instanceof ServerPlayerEntity))
            return;

        if(event.getSource().getTrueSource().getUniqueID().equals(event.getEntityLiving().getUniqueID()))
            return;

        ServerPlayerEntity player = (ServerPlayerEntity)event.getSource().getTrueSource();

        PacketsHelper.addHitMarker(player, HitMarkerType.KILL);
    }

    @SubscribeEvent
    public static void onBlock(LivingAttackEvent event) {
        if(event.getSource().getImmediateSource() instanceof PotionEntity)
            return;
        if(!(event.getSource().getTrueSource() instanceof LivingEntity))
            return;

        if(event.getSource().getTrueSource() instanceof ServerPlayerEntity) {
            LivingEntity target = event.getEntityLiving();
            ServerPlayerEntity source = (ServerPlayerEntity)event.getSource().getTrueSource();

            boolean targetIsBlocking = canBlockDamageSource(target, event.getSource());
            boolean shieldAboutToBreak = source.getHeldItemMainhand().getItem().canDisableShield(source.getHeldItemMainhand(), target.getActiveItemStack(), target, source);

            if(targetIsBlocking && shieldAboutToBreak)
                PacketsHelper.addHitMarker(source, HitMarkerType.CRIT);
        }

        if(!(event.getEntityLiving() instanceof ServerPlayerEntity))
            return;

        ServerPlayerEntity player = (ServerPlayerEntity)event.getEntityLiving();
        LivingEntity source = (LivingEntity)event.getSource().getTrueSource();

        boolean playerIsBlocking = canBlockDamageSource(player, event.getSource());
        boolean shieldAboutToBreak = source.getHeldItemMainhand().getItem().canDisableShield(source.getHeldItemMainhand(), player.getActiveItemStack(), player, source);

        if(playerIsBlocking)
            PacketsHelper.addHitIndicator(player, source, HitIndicatorType.BLUE, shieldAboutToBreak ? 125 : 0, false);
    }

    @SubscribeEvent
    public static void onPotion(ProjectileImpactEvent.Throwable event) {
        if(!(event.getThrowable().getShooter() instanceof LivingEntity)
                ||!(event.getThrowable().getShooter().getEntityWorld() instanceof ServerWorld)
                || !(event.getThrowable() instanceof PotionEntity))
            return;

        AxisAlignedBB axisalignedbb = event.getThrowable().getBoundingBox().grow(4.0D, 2.0D, 4.0D);
        List<ServerPlayerEntity> list = event.getThrowable().getEntityWorld().getEntitiesWithinAABB(ServerPlayerEntity.class, axisalignedbb);

        LivingEntity source = (LivingEntity)event.getThrowable().getShooter();
        PotionEntity potion = (PotionEntity)event.getThrowable();

        boolean hasNegativeEffects = PotionUtils.getEffectsFromStack(potion.getItem())
                .stream().anyMatch((x) -> !x.getPotion().isBeneficial());
        boolean damagingPotion = PotionUtils.getEffectsFromStack(potion.getItem())
                .stream().anyMatch((x) -> x.getPotion() == Effects.POISON
                        || x.getPotion() == Effects.INSTANT_DAMAGE
                        || x.getPotion() == Effects.WITHER);

        int damagePercent = 0;
        Optional<EffectInstance> instantDamage = PotionUtils.getEffectsFromStack(potion.getItem())
                .stream().filter((x) -> x.getPotion() == Effects.INSTANT_DAMAGE).findFirst();
        for(ServerPlayerEntity player : list) {
            if(!player.canBeHitWithPotion() || player.getUniqueID().equals(source.getUniqueID()))
                continue;

            if(damagingPotion || hasNegativeEffects) {
                if(instantDamage.isPresent()) {
                    damagePercent = (int)Math.floor(applyPotionDamageCalculations(player, DamageSource.MAGIC, 3*(2<<instantDamage.get().getAmplifier())) / player.getMaxHealth() * 100);
                }

                PacketsHelper.addHitIndicator(player, source, HitIndicatorType.RED, damagePercent, hasNegativeEffects && !damagingPotion);
            }
        }
    }

    private static boolean canBlockDamageSource(LivingEntity entity, DamageSource damageSourceIn)
    {
        if (!damageSourceIn.isUnblockable() && entity.isActiveItemStackBlocking())
        {
            Vector3d vec3d = damageSourceIn.getDamageLocation();

            if (vec3d != null)
            {
                Vector3d vec3d1 = entity.getLook(1.0F);
                Vector3d vec3d2 = vec3d.subtractReverse(new Vector3d(entity.getPosX(), entity.getPosY(), entity.getPosZ())).normalize();
                vec3d2 = new Vector3d(vec3d2.x, 0.0D, vec3d2.z);

                if (vec3d2.dotProduct(vec3d1) < 0.0D)
                {
                    return true;
                }
            }
        }

        return false;
    }

    private static float applyPotionDamageCalculations(ServerPlayerEntity player, DamageSource source, float damage)
    {
        if (source.isDamageAbsolute())
        {
            return damage;
        }
        else
        {
            if (player.isPotionActive(Effects.RESISTANCE) && source != DamageSource.OUT_OF_WORLD)
            {
                int i = (player.getActivePotionEffect(Effects.RESISTANCE).getAmplifier() + 1) * 5;
                int j = 25 - i;
                float f = damage * (float)j;
                damage = f / 25.0F;
            }

            if (damage <= 0.0F)
            {
                return 0.0F;
            }
            else
            {
                int k = EnchantmentHelper.getEnchantmentModifierDamage(player.getArmorInventoryList(), source);

                if (k > 0)
                {
                    damage = CombatRules.getDamageAfterMagicAbsorb(damage, (float)k);
                }

                return damage;
            }
        }
    }


}
