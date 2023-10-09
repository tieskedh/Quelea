/* 
 * This file is part of Quelea, free projection software for churches.
 * 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.main.actionhandlers;

import java.io.File;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.javafx.dialog.Dialog;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.library.LibraryTimerController;
import org.quelea.windows.timer.TimerIO;
import tornadofx.FX;

/**
 * Action listener that removes the selected timer from the folder
 * <p/>
 * @author Ben
 */
public class RemoveTimerActionHandler implements EventHandler<ActionEvent> {

    private boolean yes = false;

    /**
     * Remove the selected song from the database.
     * <p/>
     * @param t the action event.
     */
    @Override
    public void handle(ActionEvent t) {
        final TimerDisplayable td =  FX.find(LibraryTimerController.class).getSelectedItem();
        if (td == null) return;

        yes = false;
        Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("confirm.remove.text"),
                LabelGrabber.INSTANCE.getLabel("confirm.remove.timer").replace("$1", td.getName()))
                .addYesButton((ActionEvent t1) -> {
                    yes = true;
                }).addNoButton((ActionEvent t1) -> {
                }).build().showAndWait();

        if (yes) {
            new Thread() {
                @Override
                public void run() {
                    File f = QueleaProperties.get().getTimerDir();
                    for (int i = 0; i < f.listFiles().length; i++) {
                        File file = f.listFiles()[i];
                        TimerDisplayable t = TimerIO.timerFromFile(file);
                        if (t!=null && t.getName().equals(td.getName())) {
                            file.delete();
                            Platform.runLater(() -> {
                                FX.find(LibraryTimerController.class).refreshTimers();
                            });
                            i = f.listFiles().length + 1;
                        }
                    }
                }
            }.start();
        }
    }
}
