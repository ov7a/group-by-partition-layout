package org.ov7a.layout;

import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.openide.util.Lookup;

/**
 *
 * @author ov7a
 */
public class ColumnComboBoxEditor extends PropertyEditorSupport {

    public Map ComboValues;

    public ColumnComboBoxEditor() {
        Map options = this.getColumnEnumMap();
        ComboValues = options;
    }

    @Override
    public String[] getTags() {
        return (String[]) ComboValues.values().toArray(new String[0]);
    }

    @Override
    public String getAsText() {
        return (String) ComboValues.get(getValue());
    }

    @Override
    public void setAsText(String s) {
        Set<Map.Entry<String, String>> Entries = ComboValues.entrySet();
        for (Map.Entry<String, String> Entry : Entries) {
            if (Entry.getValue() == null ? s == null : Entry.getValue().equals(s)) {
                setValue(Entry.getKey());
            }
        }
    }

    public static Map getColumnEnumMap() {
// get all attributes
        AttributeTable at = Lookup.getDefault().lookup(AttributeController.class).getModel().getNodeTable();
        Map<String, String> map = new HashMap<String, String>();
        for (AttributeColumn c : at.getColumns()) {
// only int and double type of attributes are permited
          //  if (c.getType().equals(AttributeType.DOUBLE) || c.getType().equals(AttributeType.INT)) {
                map.put(c.getId(), c.getTitle());
          //  }
        }
        return map;
    }
}
