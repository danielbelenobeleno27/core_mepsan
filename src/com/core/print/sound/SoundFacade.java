package com.core.print.sound;

import com.core.app.NeoService;

public class SoundFacade {

    static final String SOUND_VENTA_AUTORIZADA = "sound/01_VENTA_AUTORIZADA.wav";
    static final String SOUND_SELECCION_MEDIOS = "sound/02_SELECCIONE_MEDIO_DE_IDENTIFICACION.wav";
    static final String SOUND_CONECTE_IBUTTON = "sound/03_CONECTE_EL_CHIP_IBUTTON.wav";
    static final String SOUND_CONECTE_TARJETA = "sound/04_DESLICE_LA_TARJETA.wav";
    static final String SOUND_CONECTE_TAGRFID = "sound/05_PRESENTE_TAG_RFID.wav";

    public void play(String file) {
        SoundService pl = new SoundService(file);
        pl.start();
        try {
            pl.join();
        } catch (InterruptedException ex) {
            NeoService.setLog("InterruptedException (SoundFacade.play) " + file + " " + ex.getMessage());
        }
    }
}
