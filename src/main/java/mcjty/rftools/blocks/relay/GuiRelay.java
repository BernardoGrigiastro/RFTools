package mcjty.rftools.blocks.relay;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.TypedMap;
import mcjty.rftools.RFTools;
import mcjty.rftools.setup.GuiProxy;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiRelay extends GenericGuiContainer<RelayTileEntity> {
    public static final int RELAY_WIDTH = 255;
    public static final int RELAY_HEIGHT = 148;

    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    // Maps from specific label to list of related widgets that needs to be enabled/disabled.
    private Map<String,ImageChoiceLabel> inputOutputs = new HashMap<>();
    private Map<String,TextField> energyValues = new HashMap<>();

    public GuiRelay(RelayTileEntity relayTileEntity, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, relayTileEntity, container, GuiProxy.GUI_MANUAL_MAIN, "prelay");

        xSize = RELAY_WIDTH;
        ySize = RELAY_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        toplevel.addChild(createRedstonePanel());
        for (int i = 0 ; i < 7 ; i++) {
            toplevel.addChild(createSidePanel(i));
        }

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, RELAY_WIDTH, RELAY_HEIGHT));
        window = new Window(this, toplevel);
    }

    private Panel createRedstonePanel() {
        ImageLabel redstoneOff = new ImageLabel(mc, this).setImage(iconGuiElements, 16, 0);
        redstoneOff.setDesiredWidth(16).setDesiredHeight(16).setTooltips("Redstone signal off").setLayoutHint(70, 0, 16, 16);
        ImageLabel redstoneOn = new ImageLabel(mc, this).setImage(iconGuiElements, 32, 0);
        redstoneOn.setDesiredWidth(16).setDesiredHeight(16).setTooltips("Redstone signal on").setLayoutHint(190, 0, 16, 16);
        return new Panel(mc, this).setLayout(new PositionalLayout()).
                addChildren(redstoneOff, redstoneOn);
    }

    private static final String[] tooltips = {
            "Down",
            "Up",
            "Back",
            "Front",
            "Left",
            "Right",
            "Internal (null)"
    };

    private Panel createSidePanel(int side) {
        String labelText = String.valueOf(RelayTileEntity.DUNSWEI.charAt(side));
        Label label = new Label(mc, this).setText(labelText).setTooltips(tooltips[side]);
        label.setDesiredWidth(14).setDesiredHeight(14);
        return new Panel(mc, this).setLayout(new HorizontalLayout().setHorizontalMargin(1)).
                addChild(label).
                addChild(createSubPanel(side, "Off").setDesiredWidth(115)).
                addChild(createSubPanel(side, "On").setDesiredWidth(115));
    }

    private Panel createSubPanel(int side, String redstoneState) {
        String labelText = String.valueOf(RelayTileEntity.DUNSWEI.charAt(side));

        int rf;
        boolean input;
        if ("Off".equals(redstoneState)) {
            rf = tileEntity.getRfOff(side);
            input = tileEntity.isInputModeOff(side);
        } else {
            rf = tileEntity.getRfOn(side);
            input = tileEntity.isInputModeOn(side);
        }

        ImageChoiceLabel inputOutput = new ImageChoiceLabel(mc, this).
                setDesiredWidth(14).setDesiredHeight(14).
                addChoice("Output", "Side set to output mode", iconGuiElements, 80, 16).
                addChoice("Input", "Side set to input mode", iconGuiElements, 96, 16).
                addChoiceEvent((parent, newChoice) -> changeSettings());
        String key = labelText + redstoneState;
        if (input) {
            inputOutput.setCurrentChoice("Input");
        } else {
            inputOutput.setCurrentChoice("Output");
        }
        inputOutputs.put(key, inputOutput);
        TextField energyField = new TextField(mc, this).setTooltips("Amount of RF to input/output", "when redstone is " + redstoneState).
                setDesiredWidth(42).setDesiredHeight(14).
                addTextEvent((parent, newText) -> adjustEnergy((TextField) parent, 0));
        energyField.setText(String.valueOf(rf));
        Button sub100 = createEnergyOffsetButton(energyField, "-", -500);
        Button add100 = createEnergyOffsetButton(energyField, "+", 500);
        Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout().setHorizontalMargin(1)).
                addChildren(inputOutput, sub100, energyField, add100);
        energyValues.put(key, energyField);
        return panel;
    }

    private Button createEnergyOffsetButton(final TextField energyField, String label, final int amount) {
        return new Button(mc, this).setText(label).setDesiredHeight(14).setDesiredWidth(16).addButtonEvent(parent -> adjustEnergy(energyField, amount));
    }

    private void adjustEnergy(TextField energyField, int amount) {
        int energy;
        try {
            energy = Integer.parseInt(energyField.getText());
        } catch (NumberFormatException e) {
            energy = 0;
        }
        energy = MathHelper.clamp(energy + amount, 0, 50000);
        energyField.setText(Integer.toString(energy));
        changeSettings();
    }

    private void changeSettings() {
        TypedMap.Builder builder = TypedMap.builder();
        for (int i = 0 ; i < 7 ; i++) {
            addArgument(builder, i, RelayTileEntity.PARAM_INPUTON, RelayTileEntity.PARAM_RFON, "On");
            addArgument(builder, i, RelayTileEntity.PARAM_INPUTOFF, RelayTileEntity.PARAM_RFOFF, "Off");
        }

        sendServerCommand(RFToolsMessages.INSTANCE, RelayTileEntity.CMD_SETTINGS, builder.build());
    }

    private void addArgument(TypedMap.Builder builder, int i, List<Key<Boolean>> inputKeys, List<Key<Integer>> rfKeys, String suffix) {
        char prefix = RelayTileEntity.DUNSWEI.charAt(i);
        String key = prefix + suffix;
        int energy = Integer.parseInt(energyValues.get(key).getText());
        boolean input = "Input".equals(inputOutputs.get(key).getCurrentChoice());
        builder.put(rfKeys.get(i), energy);
        builder.put(inputKeys.get(i), input);
    }
}
