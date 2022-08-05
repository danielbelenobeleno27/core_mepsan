/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.print.sound;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundService extends Thread {

    String filename;

    public SoundService(String filename) {
        this.filename = filename;
    }

    @Override
    public void run() {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new File(filename)));
            clip.start();
            while (!clip.isRunning()) {
                Thread.sleep(10);
            }
            while (clip.isRunning()) {
                Thread.sleep(10);
            }
            clip.close();
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException exc) {
            exc.printStackTrace(System.out);
        } catch (InterruptedException ex) {
            Logger.getLogger(SoundService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
