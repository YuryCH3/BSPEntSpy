package entspy;

import java.awt.Component;
import java.util.HashMap;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

public class LERenderer
extends DefaultListCellRenderer {
    static ImageIcon pointIcon;
    static ImageIcon modelIcon;
    static ImageIcon brushIcon;
    static ImageIcon triggerIcon;
    static ImageIcon lightIcon;
    static ImageIcon nodeIcon;
    static ImageIcon soundIcon;
    static ImageIcon itemIcon;
    static ImageIcon decalIcon;
    static ImageIcon logicIcon;
    static HashMap<String, ImageIcon> iconMap;
    
    static {
    	iconMap = new HashMap();
    	
    	pointIcon = new ImageIcon(LERenderer.class.getResource("/images/newicons/point.png"));
        modelIcon = new ImageIcon(LERenderer.class.getResource("/images/newicons/model.png"));
        brushIcon = new ImageIcon(LERenderer.class.getResource("/images/newicons/brush.png"));
        triggerIcon = new ImageIcon(LERenderer.class.getResource("/images/newicons/trigger.png"));
        lightIcon = new ImageIcon(LERenderer.class.getResource("/images/newicons/light.png"));
        nodeIcon = new ImageIcon(LERenderer.class.getResource("/images/newicons/node.png"));
        soundIcon = new ImageIcon(LERenderer.class.getResource("/images/newicons/sound.png"));
        itemIcon = new ImageIcon(LERenderer.class.getResource("/images/newicons/item.png"));
        decalIcon = new ImageIcon(LERenderer.class.getResource("/images/newicons/decal.png"));
        logicIcon = new ImageIcon(LERenderer.class.getResource("/images/newicons/logic.png"));
        
        iconMap.put("light", lightIcon);
        iconMap.put("light_dynamic", lightIcon);
        iconMap.put("light_spot", lightIcon);
        iconMap.put("point_spotlight", lightIcon);
        iconMap.put("ambient_generic", soundIcon);
        iconMap.put("env_soundscape", soundIcon);
        iconMap.put("env_soundscape_triggerable", soundIcon);
        iconMap.put("env_soundscape_proxy", soundIcon);
        iconMap.put("infodecal", decalIcon);
        iconMap.put("info_overlay", decalIcon);
    }

    public LERenderer() {
    }

    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean hasFocus) {
    	super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
    	this.setIcon(nodetype((Entity)value));
	    return this;
    }
    

    protected ImageIcon nodetype(Entity value) {
    	String cls = value.getKeyValue("classname");
        if(LERenderer.iconMap.containsKey(cls)) {
        	return iconMap.get(cls);
        }
        
        if(value.getKeyValue("model").startsWith("*")) {
        	if(cls.indexOf("trigger") > -1)
        		return triggerIcon;
        	
        	return brushIcon;
        } else if(!value.getKeyValue("model").equals("")) {
        	return modelIcon;
        }
        
        if(cls.indexOf("node") > -1)
    		return nodeIcon;
        
        if(cls.indexOf("item_") > -1)
    		return itemIcon;
        
        if(cls.indexOf("logic_") > -1)
    		return logicIcon;
        
        return pointIcon;
    }
}

