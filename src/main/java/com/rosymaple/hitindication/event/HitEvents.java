package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.config.HitIndicatorConfig;
import com.rosymaple.hitindication.latesthits.ClientLatestHits;
import com.rosymaple.hitindication.latesthits.HitIndicatorType;
import com.rosymaple.hitindication.latesthits.HitMarkerType;
import com.rosymaple.hitindication.latesthits.PacketsHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = HitIndication.MODID)
public class HitEvents {
    @SubscribeEvent
    public static void onAttack(LivingDamageEvent event) {
        if(event.getSource().getImmediateSource() instanceof EntityPotion)
            return;

        if(!(event.getSource().getTrueSource() instanceof EntityLivingBase)
                || event.getSource().getTrueSource().getUniqueID().equals(event.getEntity().getUniqueID())) {

            if(!(event.getEntity() instanceof EntityPlayerMP))
                return;

            EntityPlayerMP player = (EntityPlayerMP)event.getEntityLiving();
            int damagePercent = (int)Math.floor((event.getAmount() / player.getMaxHealth() * 100));

            PacketsHelper.addHitIndicator(player, null, HitIndicatorType.ND_RED, damagePercent, false);
            return;
        }

        if(event.getSource().getTrueSource() instanceof EntityPlayerMP) {
            if(event.getSource().getImmediateSource() instanceof EntityArrow)
                PacketsHelper.addHitMarker((EntityPlayerMP)event.getSource().getTrueSource(), HitMarkerType.CRIT);
        }

        if(!(event.getEntityLiving() instanceof EntityPlayerMP))
            return;

        EntityPlayerMP player = (EntityPlayerMP)event.getEntityLiving();
        EntityLivingBase source = (EntityLivingBase)event.getSource().getTrueSource();

        int damagePercent = (int)Math.floor((event.getAmount() / player.getMaxHealth() * 100));

        PacketsHelper.addHitIndicator(player, source, HitIndicatorType.RED, damagePercent, false);
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if(!(event.getEntityPlayer() instanceof EntityPlayerMP) || !event.isVanillaCritical())
            return;

        EntityPlayerMP player = (EntityPlayerMP)event.getEntityPlayer();

        PacketsHelper.addHitMarker(player, HitMarkerType.CRIT);
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if(!(event.getSource().getTrueSource() instanceof EntityPlayerMP))
            return;

        if(event.getSource().getTrueSource().getUniqueID().equals(event.getEntityLiving().getUniqueID()))
            return;

        EntityPlayerMP player = (EntityPlayerMP)event.getSource().getTrueSource();

        PacketsHelper.addHitMarker(player, HitMarkerType.KILL);
    }

    @SubscribeEvent
    public static void onBlock(LivingAttackEvent event) {
        if(event.getSource().getImmediateSource() instanceof EntityPotion)
            return;
        if(!(event.getSource().getTrueSource() instanceof EntityLivingBase))
            return;

        if(event.getSource().getTrueSource() instanceof EntityPlayerMP) {
            EntityLivingBase target = event.getEntityLiving();
            EntityPlayerMP source = (EntityPlayerMP)event.getSource().getTrueSource();

            boolean targetIsBlocking = canBlockDamageSource(target, event.getSource());
            boolean shieldAboutToBreak = source.getHeldItemMainhand().getItem().canDisableShield(source.getHeldItemMainhand(), target.getActiveItemStack(), target, source);

            if(targetIsBlocking && shieldAboutToBreak)
                PacketsHelper.addHitMarker(source, HitMarkerType.CRIT);
        }

        if(!(event.getEntityLiving() instanceof EntityPlayerMP))
            return;

        EntityPlayerMP player = (EntityPlayerMP)event.getEntityLiving();
        EntityLivingBase source = (EntityLivingBase)event.getSource().getTrueSource();

        boolean playerIsBlocking = canBlockDamageSource(player, event.getSource());
        boolean shieldAboutToBreak = source.getHeldItemMainhand().getItem().canDisableShield(source.getHeldItemMainhand(), player.getActiveItemStack(), player, source);

        if(playerIsBlocking)
            PacketsHelper.addHitIndicator(player, source, HitIndicatorType.BLUE, shieldAboutToBreak ? 125 : 0, false);
    }

    @SubscribeEvent
    public static void onPotion(ProjectileImpactEvent.Throwable event) {
        if(event.getThrowable().getThrower() == null
                ||!(event.getThrowable().getThrower().getEntityWorld() instanceof WorldServer)
                || !(event.getThrowable() instanceof EntityPotion))
            return;

        AxisAlignedBB axisalignedbb = event.getThrowable().getEntityBoundingBox().grow(4.0D, 2.0D, 4.0D);
        List<EntityPlayerMP> list = event.getThrowable().world.getEntitiesWithinAABB(EntityPlayerMP.class, axisalignedbb);

        EntityLivingBase source = event.getThrowable().getThrower();
        EntityPotion potion = (EntityPotion)event.getThrowable();

        boolean hasNegativeEffects = PotionUtils.getEffectsFromStack(potion.getPotion())
                .stream().anyMatch((x) -> x.getPotion().isBadEffect());
        boolean damagingPotion = PotionUtils.getEffectsFromStack(potion.getPotion())
                .stream().anyMatch((x) -> x.getPotion() == MobEffects.POISON
                        || x.getPotion() == MobEffects.INSTANT_DAMAGE
                        || x.getPotion() == MobEffects.WITHER);

        int damagePercent = 0;
        Optional<PotionEffect> instantDamage = PotionUtils.getEffectsFromStack(potion.getPotion())
                .stream().filter((x) -> x.getPotion() == MobEffects.INSTANT_DAMAGE).findFirst();
        for(EntityPlayerMP player : list) {
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

    private static boolean canBlockDamageSource(EntityLivingBase entity, DamageSource damageSourceIn) {
        if (!damageSourceIn.isUnblockable() && entity.isActiveItemStackBlocking())
        {
            Vec3d vec3d = damageSourceIn.getDamageLocation();

            if (vec3d != null)
            {
                Vec3d vec3d1 = entity.getLook(1.0F);
                Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(entity.posX, entity.posY, entity.posZ)).normalize();
                vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);

                if (vec3d2.dotProduct(vec3d1) < 0.0D)
                {
                    return true;
                }
            }
        }

        return false;
    }

    private static float applyPotionDamageCalculations(EntityPlayerMP player, DamageSource source, float damage) {
        if (source.isDamageAbsolute())
            return damage;

        if (player.isPotionActive(MobEffects.RESISTANCE) && source != DamageSource.OUT_OF_WORLD) {
            int i = (player.getActivePotionEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5;
            int j = 25 - i;
            float f = damage * (float)j;
            damage = f / 25.0F;
        }

        if (damage <= 0.0F)
            return 0.0F;

        int k = EnchantmentHelper.getEnchantmentModifierDamage(player.getArmorInventoryList(), source);
        if (k > 0)
            damage = CombatRules.getDamageAfterMagicAbsorb(damage, (float)k);
        return damage;
    }
}
