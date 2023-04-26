package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.capability.latesthits.Indicator;
import com.rosymaple.hitindication.capability.latesthits.LatestHitsProvider;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = HitIndication.MODID)
public class HitEvents {
    @SubscribeEvent
    public static void onAttack(LivingDamageEvent event) {
        if(!(event.getEntityLiving() instanceof ServerPlayerEntity)
                || !(event.getSource().getTrueSource() instanceof LivingEntity)
                || event.getSource().getImmediateSource() instanceof PotionEntity)
            return;

        ServerPlayerEntity player = (ServerPlayerEntity)event.getEntityLiving();
        LivingEntity source = (LivingEntity)event.getSource().getTrueSource();

        int damagePercent = (int)Math.floor((event.getAmount() / player.getMaxHealth() * 100));

        player.getCapability(LatestHitsProvider.LATEST_HITS, null).ifPresent((hits) -> {
            hits.addHit(player, source, Indicator.RED, damagePercent, false);
        });
    }

    @SubscribeEvent
    public static void onBlock(LivingAttackEvent event) {
        if(!(event.getEntityLiving() instanceof ServerPlayerEntity)
                || !(event.getSource().getTrueSource() instanceof LivingEntity)
                || event.getSource().getImmediateSource() instanceof PotionEntity)
            return;

        ServerPlayerEntity player = (ServerPlayerEntity)event.getEntityLiving();
        LivingEntity source = (LivingEntity)event.getSource().getTrueSource();

        player.getCapability(LatestHitsProvider.LATEST_HITS, null).ifPresent((hits) -> {
            boolean playerIsBlocking = canBlockDamageSource(player, event.getSource());
            boolean shieldAboutToBreak = source.getHeldItemMainhand().getItem().canDisableShield(source.getHeldItemMainhand(), player.getActiveItemStack(), player, source);
            if(playerIsBlocking) {
                hits.addHit(player, source, Indicator.BLUE, shieldAboutToBreak ? 125 : 0, false);
            }
        });
    }

    @SubscribeEvent
    public static void onPotion(ProjectileImpactEvent.Throwable event) {
        if(!(event.getThrowable().getShooter() instanceof LivingEntity)
                ||!(event.getThrowable().getShooter().getEntityWorld() instanceof ServerWorld)
                || !(event.getThrowable() instanceof PotionEntity))
            return;

        AxisAlignedBB axisalignedbb = event.getThrowable().getBoundingBox().grow(4.0D, 2.0D, 4.0D);
        List<ServerPlayerEntity> list = event.getThrowable().world.getEntitiesWithinAABB(ServerPlayerEntity.class, axisalignedbb);

        LivingEntity source = (LivingEntity)event.getThrowable().getShooter();
        PotionEntity potion = (PotionEntity)event.getThrowable();

        boolean hasNegativeEffects = PotionUtils.getEffectsFromStack(potion.getItem())
                .stream().anyMatch((x) -> !x.getPotion().isBeneficial());
        boolean damagingPotion = PotionUtils.getEffectsFromStack(potion.getItem())
                .stream().anyMatch((x) -> x.getPotion() == Effects.POISON
                        || x.getPotion() == Effects.INSTANT_DAMAGE
                        || x.getPotion() == Effects.WITHER);

        Optional<EffectInstance> instantDamage = PotionUtils.getEffectsFromStack(potion.getItem())
                .stream().filter((x) -> x.getPotion() == Effects.INSTANT_DAMAGE).findFirst();
        for(ServerPlayerEntity player : list) {
            if(!player.canBeHitWithPotion())
                continue;

            player.getCapability(LatestHitsProvider.LATEST_HITS, null).ifPresent((hits) -> {
                int damagePercent = 0;
                if(damagingPotion || hasNegativeEffects) {
                    if(instantDamage.isPresent()) {
                        damagePercent = (int)Math.floor(applyPotionDamageCalculations(player, DamageSource.MAGIC, 3*(2<<instantDamage.get().getAmplifier())) / player.getMaxHealth() * 100);
                    }

                    hits.addHit(player, source, Indicator.RED, damagePercent, hasNegativeEffects);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if(!(event.player instanceof ServerPlayerEntity) || event.phase == TickEvent.Phase.END)
            return;

        event.player.getCapability(LatestHitsProvider.LATEST_HITS, null).ifPresent((hits) -> {
            hits.tick((ServerPlayerEntity)event.player);
        });
    }

    private static boolean canBlockDamageSource(ServerPlayerEntity entity, DamageSource damageSourceIn)
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
