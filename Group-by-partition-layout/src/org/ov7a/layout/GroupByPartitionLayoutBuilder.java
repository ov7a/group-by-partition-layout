package org.ov7a.layout;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ov7a
 */
@ServiceProvider(service = LayoutBuilder.class)
public class GroupByPartitionLayoutBuilder implements LayoutBuilder {

    @Override
    public String getName() {
        return "Group by partition";
    }

    @Override
    public LayoutUI getUI() {
        return new LayoutUI() {
            @Override
            public String getDescription() {
                return "FIXME";
            }

            @Override
            public Icon getIcon() {
                return null;
            }

            @Override
            public JPanel getSimplePanel(Layout layout) {
                return null;
            }

            @Override
            public int getQualityRank() {
                return -1;
            }

            @Override
            public int getSpeedRank() {
                return -1;
            }
        };
    }

    @Override
    public Layout buildLayout() {
        return new GroupByPartitionLayout(this);
    }

}
