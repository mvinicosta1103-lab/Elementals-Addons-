package dev.saperate.elementals.elements.plant;

import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.data.PlayerData;
import dev.saperate.elementals.elements.Element;
import dev.saperate.elementals.elements.Upgrade;

/**
 * PlantElement ("Plant")
 * <p>
 * Subbending de Water: fusão de Water (a seiva/água que corre pelas plantas)
 * com o controle vegetal em si — assim como Mud é a fusão de Earth + Water.
 * Segue exatamente o mesmo padrão de Mud/Crystal deste addon: um Element
 * totalmente novo, registrado pelo próprio mod (não depende de nenhum jar
 * externo), com exatamente 4 ramos na raiz (limite da UpgradeTreeScreen) e
 * as habilidades bônus anexadas como filhos dentro dos ramos existentes em
 * vez de ocuparem um 5º/6º slot na raiz.
 */
public class PlantElement extends Element {

    public PlantElement() {
        super("Plant", new Upgrade[]{
                // Ramo 1 — Ataque: chicote de vinhas que puxa e machuca o alvo,
                // mais a habilidade bônus "vineGrapple" (puxa o próprio jogador
                // até um bloco/alvo, como um gancho)
                new Upgrade("vineWhip", new Upgrade[]{
                        new Upgrade("vineWhipRangeI", new Upgrade[]{
                                new Upgrade("vineWhipRangeII", 2)
                        }, 1),
                        new Upgrade("vineWhipDamageI", 1),
                        new Upgrade("vineGrapple", new Upgrade[]{
                                new Upgrade("vineGrapplePowerI", 1)
                        }, 2)
                }, 2),

                // Ramo 2 — Controle: raízes que brotam do chão numa área e
                // prendem quem estiver em cima (mesmo padrão de quicksand)
                new Upgrade("plantRoots", new Upgrade[]{
                        new Upgrade("plantRootsRadiusI", new Upgrade[]{
                                new Upgrade("plantRootsRadiusII", 2)
                        }, 1),
                        new Upgrade("plantRootsGripI", 2)
                }, 3),

                // Ramo 3 — Defesa: parede de vinhas que sobe do chão, mais a
                // habilidade bônus "thornSkin" (casca de espinhos, resistência
                // a dano por um tempo)
                new Upgrade("vineWall", new Upgrade[]{
                        new Upgrade("vineWallHeightI", new Upgrade[]{
                                new Upgrade("vineWallHeightII", 2)
                        }, 1),
                        new Upgrade("vineWallWidthI", 1),
                        new Upgrade("thornSkin", new Upgrade[]{
                                new Upgrade("thornSkinDurationI", 1)
                        }, 2)
                }, 2),

                // Ramo 4 — Suporte: fotossíntese cura o jogador aos poucos,
                // com o capstone plantMastery aninhado aqui pra não precisar
                // de um 5º ramo na raiz
                new Upgrade("photosynthesis", new Upgrade[]{
                        new Upgrade("photosynthesisPowerI", new Upgrade[]{
                                new Upgrade("photosynthesisPowerII", 2)
                        }, 1),
                        new Upgrade("photosynthesisRangeI", new Upgrade[]{
                                new Upgrade("plantMastery", 4)
                        }, 1)
                }, 3)
        });

        this.addAbility(new VineWhipAbility(), true);
        this.addAbility(new PlantRootsAbility(), true);
        this.addAbility(new VineWallAbility(), true);
        this.addAbility(new PhotosynthesisAbility(), true);

        // Bônus (slots 4 e 5) — mesmo padrão de índice fixo usado por Mud
        // pra habilidades bônus que ficam dentro de ramos já existentes.
        this.addAbility(new VineGrappleAbility(), 4);
        this.addAbility(new ThornSkinAbility(), 5);
        this.registerUpgradeKeybind("vineGrapple", 4);
        this.registerUpgradeKeybind("thornSkin", 5);
    }

    public static Element get() {
        return PlantElement.getElement("Plant");
    }

    @Override
    public boolean isSkillTreeComplete(Bender bender) {
        PlayerData data = bender.plrData;
        Element water = Element.getElement("Water");
        return water != null
                && bender.hasElement(water)
                && data.canUseUpgrade("vineWhipRangeII")
                && data.canUseUpgrade("vineWhipDamageI")
                && data.canUseUpgrade("vineGrapplePowerI")
                && data.canUseUpgrade("plantRootsRadiusII")
                && data.canUseUpgrade("plantRootsGripI")
                && data.canUseUpgrade("vineWallHeightII")
                && data.canUseUpgrade("vineWallWidthI")
                && data.canUseUpgrade("thornSkinDurationI")
                && data.canUseUpgrade("photosynthesisPowerII")
                && data.canUseUpgrade("plantMastery");
    }

    // Tons de verde-folha (primário, secundário, terciário)
    @Override
    public int getColor() {
        return 0xFF3C8A3C;
    }

    @Override
    public int getSecondaryColor() {
        return 0xFF2C6B2C;
    }

    @Override
    public int getTertiaryColor() {
        return 0xFF1B4D1B;
    }
}