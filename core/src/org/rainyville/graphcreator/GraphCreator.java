package org.rainyville.graphcreator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GraphCreator extends ApplicationAdapter {
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private List<Node> nodeList;
    private BitmapFont font;
    private EditorMode mode;
    private boolean delete;
    private Random random;
    private GlyphLayout layout;

    private Node selectedNode;
    private int selectedIndex;

    private AdjacencyMatrix matrix;

    @Override
    public void create() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        nodeList = new ArrayList<>();
        mode = EditorMode.VERTEX;
        random = new Random();
        layout = new GlyphLayout();

        matrix = new AdjacencyMatrix();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                setSelectedNode(null);
                if (keycode == Input.Keys.I) {
                    mode = mode.next();
                } else if (keycode == Input.Keys.FORWARD_DEL) {
                    delete = !delete;
                }
                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector3 touchPos = new Vector3();
                touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                Vector3 real = camera.unproject(touchPos);

                Node node = matchNode();

                // We have clicked a node.
                if (node != null) {
                    if (mode == EditorMode.COLOR) {
                        node.color = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1F);
                    } else if (mode == EditorMode.EDGE) {
                        if (selectedNode == null)
                            setSelectedNode(node);
                        else {
                            int from = selectedIndex;
                            setSelectedNode(node);
                            int to = selectedIndex;

                            if (delete)
                                matrix.removeEdge(from, to);
                            else
                                matrix.addEdge(from, to);

                            setSelectedNode(null);
                        }
                    } else if (mode == EditorMode.VERTEX && delete) {
                        setSelectedNode(node);
                        removeSelectedNode();
                    } else if (mode == EditorMode.NONE) {
                        setSelectedNode(node);
                    }
                } else { // We didn't find a node.
                    if (mode == EditorMode.VERTEX && !delete) {
                        Node newNode = new Node();
                        newNode.x = real.x;
                        newNode.y = real.y;
                        newNode.radius = 15;

                        boolean overlaps = false;
                        for (Node check : nodeList) {
                            if (newNode.overlaps(check)) {
                                overlaps = true;
                                break;
                            }
                        }

                        if (!overlaps)
                            addNode(newNode);
                    } else if (mode == EditorMode.NONE && selectedNode != null) {
                        selectedNode.x = real.x;
                        selectedNode.y = real.y;
                    }
                }

                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                Vector3 touchPos = new Vector3();
                touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                Vector3 real = camera.unproject(touchPos);

                if (mode == EditorMode.NONE && selectedNode != null) {
                    selectedNode.x = real.x;
                    selectedNode.y = real.y;
                }

                return true;
            }
        });

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("VCR.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.shadowColor = Color.BLACK;
        parameter.shadowOffsetX = 2;
        parameter.shadowOffsetY = 2;
        parameter.size = 18;
        parameter.flip = false;
        font = generator.generateFont(parameter);
        font.getData().markupEnabled = true;
        generator.dispose();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.0F, 0.3F, 0.5F, 1F);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        for (int i = 0; i < nodeList.size(); i++) {
            Node node = nodeList.get(i);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(node.color);
            shapeRenderer.circle(node.x, node.y, node.radius);
            shapeRenderer.end();

            StringBuilder builder = new StringBuilder();

            List<Integer> connected = new ArrayList<>(2);
            int[][] array = matrix.getMatrix();
            builder.append("v").append(i).append(": ");
            for (int edge = 0; edge < array[i].length; edge++) {
                connected.clear();
                if (array[i][edge] == 1) {
                    builder.append('e').append(edge).append(',');

                    for (int vertex = 0; vertex < array.length; vertex++) {
                        if (array[vertex][edge] == 1) {
                            connected.add(vertex);
                        }
                    }
                    if (connected.size() == 1) {
                        Gdx.gl.glLineWidth(3);
                        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                        shapeRenderer.setColor(Color.BLACK);
                        shapeRenderer.circle(node.x + 7.5F, node.y + 7.5F, 15);
                        shapeRenderer.end();
                    } else if (!connected.isEmpty()) {
                        Node start = nodeList.get(connected.get(0));
                        Node end = nodeList.get(connected.get(1));
                        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                        shapeRenderer.setColor(Color.BLACK);
                        shapeRenderer.rectLine(start.x, start.y, end.x, end.y, 5);
                        shapeRenderer.end();
                    }
                }
            }

            batch.begin();
            font.draw(batch, builder.substring(0, builder.length() - 1), 2, 18 * (i + 1));
            font.draw(batch, String.format("[CYAN]v%d", i), node.x - 12, node.y + 7);
            batch.end();
        }

        batch.begin();

        font.draw(batch, String.format("Vertices: %d, edges: %d", matrix.getVertices(), matrix.getEdges()), 2, Gdx.graphics.getHeight() - 20);
        String text = String.format("FPS: %d", Gdx.graphics.getFramesPerSecond());

        layout.setText(font, text);
        font.draw(batch, text, Gdx.graphics.getWidth() - layout.width, Gdx.graphics.getHeight());
        if (selectedNode != null)
            font.draw(batch, String.format("Selected Node: v%d", selectedIndex), 2, Gdx.graphics.getHeight() - 40);
        batch.end();

        batch.begin();
        font.draw(batch, String.format("Mode: %s%s", delete && mode.hasDelete() ? "[RED]" : "[GREEN]", mode), 2, Gdx.graphics.getHeight());
        batch.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
    }

    private Node matchNode() {
        Vector3 touchPos = new Vector3();
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        Vector3 real = camera.unproject(touchPos);

        Node clickPoint = new Node();
        clickPoint.x = real.x;
        clickPoint.y = real.y;
        clickPoint.radius = 15;

        for (Node node : nodeList) {
            if (node.overlaps(clickPoint))
                return node;
        }

        return null;
    }

    private void setSelectedNode(Node selected) {
        this.selectedNode = selected;
        this.selectedIndex = -1;
        if (selectedNode == null) return;

        for (int i = 0; i < nodeList.size(); i++) {
            Node node = nodeList.get(i);
            if (node.equals(selectedNode)) {
                selectedIndex = i;
                break;
            }
        }
    }

    private void addNode(Node node) {
        nodeList.add(node);
        matrix.addVertex();
    }

    private void removeSelectedNode() {
        nodeList.remove(selectedIndex);
        matrix.removeVertex(selectedIndex);
        setSelectedNode(null);
    }
}
