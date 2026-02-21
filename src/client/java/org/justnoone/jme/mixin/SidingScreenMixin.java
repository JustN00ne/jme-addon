package org.justnoone.jme.mixin;

import net.minecraft.client.gui.widget.ClickableWidget;
import org.justnoone.jme.accessor.SidingMaxSpeedAccess;
import org.mtr.core.data.Siding;
import org.mtr.core.data.TransportMode;
import org.mtr.mapping.mapper.CheckboxWidgetExtension;
import org.mtr.mapping.mapper.ScreenExtension;
import org.mtr.mapping.mapper.TextFieldWidgetExtension;
import org.mtr.mapping.tool.TextCase;
import org.mtr.mod.screen.SidingScreen;
import org.mtr.mod.screen.WidgetShorterSlider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SidingScreen.class)
public abstract class SidingScreenMixin {

    @Unique
    private static final int JME_MAX_SPEED_LIMIT = 500;

    @Shadow
    private WidgetShorterSlider sliderDelayedVehicleReduceDwellTimePercentage;

    @Shadow
    private CheckboxWidgetExtension buttonEarlyVehicleIncreaseDwellTime;

    @Unique
    private Siding jme$siding;

    @Unique
    private WidgetShorterSlider jme$maxSpeedSlider;

    @Unique
    private TextFieldWidgetExtension jme$maxSpeedInput;

    @Unique
    private boolean jme$updatingWidgets;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void jme$storeSiding(Siding siding, TransportMode transportMode, ScreenExtension previousScreenExtension, CallbackInfo ci) {
        jme$siding = siding;
    }

    @Inject(method = "init2", at = @At("TAIL"))
    private void jme$addMaxSpeedControls(CallbackInfo ci) {
        jme$ensureControls();
    }

    @Inject(method = "tick2", at = @At("TAIL"))
    private void jme$tickMaxSpeedInput(CallbackInfo ci) {
        jme$ensureControls();
        jme$repositionControls();
        if (jme$maxSpeedInput != null) {
            jme$maxSpeedInput.tick2();
        }
    }

    @Unique
    private void jme$ensureControls() {
        final SidingMaxSpeedAccess sidingMaxSpeedAccess = jme$getSidingMaxSpeedAccess();
        if (sidingMaxSpeedAccess == null || sliderDelayedVehicleReduceDwellTimePercentage == null) {
            return;
        }

        if (jme$maxSpeedSlider == null) {
            final int x = sliderDelayedVehicleReduceDwellTimePercentage.getX2();
            final int y = buttonEarlyVehicleIncreaseDwellTime == null ? sliderDelayedVehicleReduceDwellTimePercentage.getY2() + 24 : buttonEarlyVehicleIncreaseDwellTime.getY2() - 24;
            final int width = sliderDelayedVehicleReduceDwellTimePercentage.getWidth();

            jme$maxSpeedSlider = new WidgetShorterSlider(
                    x,
                    y,
                    width,
                    JME_MAX_SPEED_LIMIT,
                    20,
                    this::jme$getSliderText,
                    sliderValue -> {
                        if (!jme$updatingWidgets) {
                            jme$applyMaxSpeed(sliderValue);
                        }
                    }
            );

            jme$maxSpeedInput = new TextFieldWidgetExtension(
                    x + width + 4,
                    y,
                    56,
                    20,
                    3,
                    TextCase.DEFAULT,
                    "[^\\d]",
                    ""
            );
            jme$maxSpeedInput.setMaxLength2(3);
            jme$maxSpeedInput.setChangedListener2(this::jme$onInputChange);

            ((ScreenInvokerMixin) (Object) this).jme$invokeAddDrawableChild((ClickableWidget) (Object) jme$maxSpeedSlider);
            ((ScreenInvokerMixin) (Object) this).jme$invokeAddDrawableChild((ClickableWidget) (Object) jme$maxSpeedInput);
            jme$syncWidgets(sidingMaxSpeedAccess.jme$getMaxSpeedKph());
        }
    }

    @Unique
    private String jme$getSliderText(int value) {
        return value <= 0 ? "Line speed cap: Unlimited" : "Line speed cap: " + value + " km/h";
    }

    @Unique
    private void jme$onInputChange(String text) {
        if (jme$updatingWidgets || jme$siding == null) {
            return;
        }

        if (text == null || text.isEmpty()) {
            jme$applyMaxSpeed(0);
            return;
        }

        try {
            jme$applyMaxSpeed(Integer.parseInt(text));
        } catch (Exception ignored) {
        }
    }

    @Unique
    private void jme$applyMaxSpeed(int value) {
        final SidingMaxSpeedAccess sidingMaxSpeedAccess = jme$getSidingMaxSpeedAccess();
        if (sidingMaxSpeedAccess == null) {
            return;
        }
        final int clampedValue = Math.max(0, Math.min(JME_MAX_SPEED_LIMIT, value));
        sidingMaxSpeedAccess.jme$setMaxSpeedKph(clampedValue);
        jme$syncWidgets(clampedValue);
    }

    @Unique
    private SidingMaxSpeedAccess jme$getSidingMaxSpeedAccess() {
        if (jme$siding == null) {
            return null;
        }
        try {
            return (SidingMaxSpeedAccess) (Object) jme$siding;
        } catch (ClassCastException ignored) {
            return null;
        }
    }

    @Unique
    private void jme$syncWidgets(int value) {
        jme$updatingWidgets = true;
        if (jme$maxSpeedSlider != null) {
            jme$maxSpeedSlider.setValue(value);
        }
        if (jme$maxSpeedInput != null) {
            jme$maxSpeedInput.setText2(value <= 0 ? "" : String.valueOf(value));
        }
        jme$updatingWidgets = false;
    }

    @Unique
    private void jme$repositionControls() {
        if (jme$maxSpeedSlider == null || jme$maxSpeedInput == null || sliderDelayedVehicleReduceDwellTimePercentage == null) {
            return;
        }

        final int x = sliderDelayedVehicleReduceDwellTimePercentage.getX2();
        final int y = buttonEarlyVehicleIncreaseDwellTime == null ? sliderDelayedVehicleReduceDwellTimePercentage.getY2() + 24 : buttonEarlyVehicleIncreaseDwellTime.getY2() - 24;
        final int width = sliderDelayedVehicleReduceDwellTimePercentage.getWidth();

        jme$maxSpeedSlider.setX2(x);
        jme$maxSpeedSlider.setY2(y);
        jme$maxSpeedSlider.setWidth2(width);

        jme$maxSpeedInput.setX2(x + width + 4);
        jme$maxSpeedInput.setY2(y);
    }

}
