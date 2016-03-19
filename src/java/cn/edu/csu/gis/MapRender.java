package cn.edu.csu.gis;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author pcwang
 */
public class MapRender {

    protected int _width, _height;
    protected double _minx, _miny, _maxx, _maxy;
    protected String _format;
    protected int _srid = 4326;

    protected BufferedImage _bufImage = null;
    protected Graphics2D _graphics = null;
    protected WKTReader _reader = null;
    protected double _cx, _cy, _scale;
    private String[] _layers;

    public MapRender(int width, int height, String format) {
        _width = width;
        _height = height;
        _format = format;
        _bufImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);

        _graphics = _bufImage.createGraphics();
        _reader = new WKTReader();
        _graphics.setColor(Color.black);
    }

    public void setExtent(double minx, double miny, double maxx, double maxy) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(
                    "jdbc:postgresql://127.0.0.1:5432/gisdb",
                    "Administrator", "");
            String sql = "select "
                    + "st_x(st_transform("
                    + "     'SRID=4214;POINT(" + String.valueOf(minx) + " " + String.valueOf(miny) + ")'::geometry,"
                    + "?)) as x, "
                    + "st_y(st_transform("
                    + "     'SRID=4214;POINT(" + String.valueOf(minx) + " " + String.valueOf(miny) + ")'::geometry,"
                    + "?)) as y";
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, _srid);
            st.setInt(2, _srid);

            ResultSet rs = st.executeQuery();
            rs.next();
            _minx = rs.getDouble(1);
            _miny = rs.getDouble(2);

            rs.close();
            st.close();

            sql = "select "
                    + "st_x(st_transform("
                    + "     'SRID=4214;POINT(" + String.valueOf(maxx) + " " + String.valueOf(maxy) + ")'::geometry,"
                    + "?)) as x, "
                    + "st_y(st_transform("
                    + "     'SRID=4214;POINT(" + String.valueOf(maxx) + " " + String.valueOf(maxy) + ")'::geometry,"
                    + "?)) as y";
            st = conn.prepareStatement(sql);
            st.setInt(1, _srid);
            st.setInt(2, _srid);

            rs = st.executeQuery();
            rs.next();
            _maxx = rs.getDouble(1);
            _maxy = rs.getDouble(2);

            _cx = (_minx + _maxx) * 0.5;
            _cy = (_miny + _maxy) * 0.5;
            double sx = _width / (_maxx - _minx);
            double sy = _height / (_maxy - _miny);
            _scale = sx < sy ? sx : sy;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MapRender.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MapRender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void render() {
        try {
            double a = _scale;
            double b = 0;
            double d = 0;
            double e = -_scale;
            double xoff = _width * 0.5 - _cx * _scale;
            double yoff = _height * 0.5 + _cy * _scale;

            //connect to db
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(
                    "jdbc:postgresql://127.0.0.1:5432/gisdb",
                    "Administrator", "");
            String sql = "select name,tablename,geoname from china";
            PreparedStatement st = conn.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            ArrayList<String> names = new ArrayList<>();
            ArrayList<String> tablenames = new ArrayList<>();
            ArrayList<String> geonames = new ArrayList<>();

            while (rs.next()) {
                names.add(rs.getString(1));
                tablenames.add(rs.getString(2));
                geonames.add(rs.getString(3));
            }
            rs.close();
            st.close();
            for (int i = 0; i < names.size(); i++) {
                sql = ""
                        + "select st_astext(st_affine(st_transform(" + geonames.get(i) + ",?),?,?,?,?,?,?))  as geo from  "
                        + tablenames.get(i);
                st = conn.prepareStatement(sql);
                int j = 1;
                st.setInt(j++, _srid);
                st.setDouble(j++, a);
                st.setDouble(j++, b);
                st.setDouble(j++, d);
                st.setDouble(j++, e);
                st.setDouble(j++, xoff);
                st.setDouble(j++, yoff);
                rs = st.executeQuery();

                while (rs.next()) {
                    String wkt = rs.getString(1);
                    Geometry geo = _reader.read(wkt);
                    draw(geo);
                }
                rs.close();
                st.close();
            }
            // construct sql statements and send it to dbserver
            // get the results from db and render the geometries
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MapRender.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MapRender.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(MapRender.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void saveTo(OutputStream out) throws IOException {
        ImageIO.write(_bufImage, _format, out);
    }

    private void draw(Geometry geo) {
        String type = geo.getGeometryType();
        if (type == "Point") {
            drawPoint((Point) geo);
        } else if (type == "LineString") {
            drawLineString((LineString) geo);
        } else if (type == "Polygon") {
            drawPolygon((Polygon) geo);
        } else if (type == "MultiLineString") {
            drawMultiLineString((MultiLineString) geo);
        }

    }

    private void drawPoint(Point point) {
        _graphics.drawOval((int) (point.getX() - 5),
                (int) (point.getY() - 5), 10, 10);
    }

    private void drawLineString(LineString lineString) {
        int nPoints = lineString.getNumPoints();
        int[] xPoints = new int[nPoints];
        int[] yPoints = new int[nPoints];
        for (int i = 0; i < nPoints; i++) {
            Point pt = lineString.getPointN(i);
            xPoints[i] = (int) pt.getX();
            yPoints[i] = (int) pt.getY();
        }
        _graphics.setColor(Color.red);
        _graphics.drawPolyline(xPoints, yPoints, nPoints);
    }

    private void drawPolygon(Polygon polygon) {
        int nPoints1 = polygon.getNumPoints();
        int[] xPoints1 = new int[nPoints1];
        int[] yPoints1 = new int[nPoints1];
        for (int i = 0; i < nPoints1; i++) {
            Point pt = polygon.getInteriorPoint();
            xPoints1[i] = (int) pt.getX();
            yPoints1[i] = (int) pt.getY();
        }
        _graphics.setColor(Color.blue);
        _graphics.fillPolygon(xPoints1, yPoints1, nPoints1);
    }

    private void drawMultiLineString(MultiLineString mls) {
        int nl = mls.getNumGeometries();
        for (int i = 0; i < nl; i++) {
            LineString ls = (LineString) mls.getGeometryN(i);
            drawLineString(ls);
        }
    }

    public void setLayers(String[] layers) {
        _layers = layers;
    }

    public void setSrid(int srid) {
        _srid = srid;
    }

}
