/**
 * Copyright (c) 2013, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package impl.org.controlsfx.skin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.ButtonBar.ButtonType;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

public class ButtonBarSkin extends BehaviorSkinBase<ButtonBar, BehaviorBase<ButtonBar>> {
    
    /**************************************************************************
     * 
     * Static fields
     * 
     **************************************************************************/

    private static String CATEGORIZED_TYPES = "LRHEYNXBIACO";
    
    // represented as a ButtonType
    public static String BUTTON_TYPE_PROPERTY = "controlfx.button.type";
    
    
    
    /**************************************************************************
     * 
     * fields
     * 
     **************************************************************************/
    
    private HBox hbox;
    
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/

    public ButtonBarSkin(final ButtonBar control) {
        super(control, new BehaviorBase<>(control));
        
        // TODO: the gap has to be OS dependent
        this.hbox = new HBox(10);
        getChildren().add(hbox);
        
        layoutButtons();
        
        control.getButtons().addListener( new ListChangeListener<ButtonBase>() {
            @Override public void onChanged(ListChangeListener.Change<? extends ButtonBase> change) {
                layoutButtons();
            }
        });
        
        registerChangeListener(control.buttonOrderProperty(), "BUTTON_ORDER");
    }
    
    
    
    /**************************************************************************
     * 
     * Overriding public API
     * 
     **************************************************************************/
    
    @Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        
        if ("BUTTON_ORDER".equals(p)) {
            layoutButtons();
        }
    }
    
    
    
    /**************************************************************************
     * 
     * Implementation
     * 
     **************************************************************************/
    
    private void layoutButtons() {
        List<? extends ButtonBase> buttons = getSkinnable().getButtons();
        
        hbox.getChildren().clear();
        
        Map<String, List<ButtonBase>> buttonMap = buildButtonMap(buttons);
        SpacerType preparedSpacer = SpacerType.NONE;
        for ( char type: getSkinnable().getButtonOrder().toCharArray()) {
            
            if ( type == '+') {
               preparedSpacer = preparedSpacer.replace(SpacerType.DYNAMIC);
            } else if ( type == '_') {
               preparedSpacer = preparedSpacer.replace(SpacerType.FIXED);
            } else {
                
                List<ButtonBase> buttonList = buttonMap.get(String.valueOf(type).toUpperCase());
                if ( buttonList != null ) {
                    Node spacer = preparedSpacer.create();
                    if ( spacer != null ) {
                        hbox.getChildren().add(spacer);
                        preparedSpacer = preparedSpacer.replace(SpacerType.NONE);
                    }
                    
                    for( ButtonBase btn: buttonList) {
                        hbox.getChildren().add(btn);
                        HBox.setHgrow(btn, Priority.ALWAYS);
                    }
                } 
            }
        }
        
    }
    
    private String getButtonType( ButtonBase btn ) {
        ButtonType buttonType =  (ButtonType) btn.getProperties().get(BUTTON_TYPE_PROPERTY);
        String typeCode = buttonType.getTypeCode();
        typeCode = typeCode.length() > 0? typeCode.substring(0,1): "";
        return CATEGORIZED_TYPES.contains(typeCode.toUpperCase())? typeCode : "U"; 
    }
    
    private Map<String, List<ButtonBase>> buildButtonMap( List<? extends ButtonBase> buttons ) {
        
        Map<String, List<ButtonBase>> buttonMap = new HashMap<>();
        for (ButtonBase btn : buttons) {
            if ( btn == null ) continue;
            String type =  getButtonType(btn); 
            List<ButtonBase> typedButtons = buttonMap.get(type);
            if ( typedButtons == null ) {
                typedButtons = new ArrayList<ButtonBase>();
                buttonMap.put(type, typedButtons);
            }
            typedButtons.add( btn );
        }
        return buttonMap;
        
    }

    
    
    /**************************************************************************
     * 
     * Support classes / enums
     * 
     **************************************************************************/
    
    private enum SpacerType {
        FIXED {
            @Override public Node create() {
                Region spacer = new Region();
                spacer.setMinWidth(10);
                HBox.setHgrow(spacer, Priority.NEVER);
                return spacer;
            }
            
            @Override public SpacerType replace( SpacerType type ) {
                return type == NONE || type == DYNAMIC? type: this;
            }
        },
        DYNAMIC {
            @Override public Node create() {
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                return spacer;
            }
            
            @Override public SpacerType replace( SpacerType type ) {
                return type == NONE? type: this;
            }
        },
        NONE {
            @Override public SpacerType replace( SpacerType type ) {
                return type == FIXED || type == DYNAMIC? type: this;
            }
        };
        
        public abstract SpacerType replace( SpacerType type );
        
        public Node create() {
            return null;
        }
    }
}