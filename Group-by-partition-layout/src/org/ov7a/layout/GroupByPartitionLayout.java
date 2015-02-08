package org.ov7a.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.Lookup;

public class GroupByPartitionLayout implements Layout {

    private final LayoutBuilder layoutBuilder;
    private GraphModel graphModel;
    private Graph graph;
    private String selectedColumn;
    private boolean converged;

    public GroupByPartitionLayout(LayoutBuilder layoutBuilder) {
        this.layoutBuilder = layoutBuilder;
        this.converged = true;
    }

    @Override
    public void initAlgo() {
        this.graph = graphModel.getGraphVisible();
        this.converged = false;
    }

    @Override
    public void setGraphModel(GraphModel graphModel) {
        this.graphModel = graphModel;
        graph = graphModel.getHierarchicalDirectedGraphVisible();
    }

    @Override
    public void goAlgo() {
        graph = graphModel.getGraphVisible();
        AttributeTable at = Lookup.getDefault().lookup(AttributeController.class).getModel().getNodeTable();
        AttributeColumn column = at.getColumn(selectedColumn);
        Node[] nodes = graph.getNodes().toArray();
        if (nodes.length <= 0 || selectedColumn == null) {
            this.converged = true;
            return;
        }
        //assume all nodes have same size
        float nodeSize = nodes[0].getNodeData().getSize();

        HashMap<String, ArrayList<Node>> groupedNodes = GroupByPartition(nodes, column);
        int groupsCount = groupedNodes.size();
        //get max size of box 
        //get original center x = max_x-min_x;
        float maxBoxSize = 0.0f;
        float maxX = nodes[0].getNodeData().x(), minX = maxX;
        float maxY = nodes[0].getNodeData().y(), minY = maxY;

        for (ArrayList<Node> al : groupedNodes.values()) {
            float boxSize = al.size() * nodeSize;
            if (boxSize > maxBoxSize) {
                maxBoxSize = boxSize;
            }
            for (Node n : al) {
                float x = n.getNodeData().x();
                float y = n.getNodeData().y();
                if (x > maxX) {
                    maxX = x;
                }
                if (y > maxY) {
                    maxY = y;
                }
                if (x < minX) {
                    minX = x;
                }
                if (y < minY) {
                    minY = y;
                }
            }
        }

        //calculate radius l = 2piR, l=2nr => r = nr/pi
        float centerX = (maxX + minX) / 2;
        float centerY = (maxY + minY) / 2;

        //place boxes as circle
        Iterator<Coordinates> boxesCoordinates = this.arrangeAsCircle(groupsCount, centerX, centerY, maxBoxSize, maxBoxSize);        
        for (ArrayList<Node> al : groupedNodes.values()) {
            Coordinates boxCoordinates = boxesCoordinates.next();
            Iterator<Coordinates> nodesCoordinates = this.arrangeAsCircle(al.size(), boxCoordinates.x, boxCoordinates.y, nodeSize, nodeSize);
            //place nodes in boxes     
            for (Node n : al) {
                Coordinates nodeCoordinates = nodesCoordinates.next();
                n.getNodeData().setX(nodeCoordinates.x);
                n.getNodeData().setY(nodeCoordinates.y);
            }
        }

        this.converged = true;
    }

    @Override
    public boolean canAlgo() {
        return !this.converged && graphModel != null && selectedColumn != null;
    }

    @Override
    public void endAlgo() {
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        try {
            properties.add(LayoutProperty.createProperty(
                    this,
                    String.class,
                    "Attribute",
                    null,
                    "select attribute",
                    "getColumn",
                    "setColumn",
                    ColumnComboBoxEditor.class));
            /**
             * properties.add(LayoutProperty.createProperty( this, Double.class,
             * "Layer Distance", null, "Distance between each layer",
             * "getLayerDistance", "setLayerDistance"));
             * properties.add(LayoutProperty.createProperty( this,
             * Boolean.class, "Adjust", null, "Adjust by size", "isAdjust",
             * "setAdjust"));
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public void resetPropertiesValues() {
//fixme
    }

    @Override
    public LayoutBuilder getBuilder() {
        return layoutBuilder;
    }

    private HashMap<String, ArrayList<Node>> GroupByPartition(Node[] nodes, AttributeColumn column) {
        HashMap<String, ArrayList<Node>> result = new HashMap();
        for (Node n : nodes) {
            String curValue = getAsString(n, column);
            if (!result.containsKey(curValue)) {
                result.put(curValue, new ArrayList<Node>());
            }
            result.get(curValue).add(n);
        }
        return result;
    }

    public String getAsString(Node o1, AttributeColumn column) {
        Object value = (((AttributeRow) (o1.getNodeData().getAttributes())).getValue(column));
        if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }
    
    public double getAsDouble(Node o1, AttributeColumn column) {
        Object value = (((AttributeRow) (o1.getNodeData().getAttributes())).getValue(column));
        if (value == null) {
            return Double.NaN;
        } else {
            return Double.parseDouble(value.toString());
        }
    }

    private Iterator<Coordinates> arrangeAsCircle(final float count, final float centerX, final float centerY, final float sizeX, final float sizeY) {
        return new Iterator<Coordinates>() {
            final double angle = 2 * Math.PI / count;
            final double offsetAngle = Math.PI / 2;
            final float RX = count * sizeX;
            final float RY = count * sizeY;

            int index = 0;

            @Override
            public boolean hasNext() {
                return index < count;
            }

            @Override
            public Coordinates next() {
                float x = (float) (centerX + RX * Math.cos(offsetAngle + angle * index));
                float y = (float) (centerY + RY * Math.sin(offsetAngle + angle * index));
                index++;
                return new Coordinates(x, y);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public String getColumn() {
        return this.selectedColumn;
    }

    public void setColumn(String column) {
        this.selectedColumn = column;
    }
}
