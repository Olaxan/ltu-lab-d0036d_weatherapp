package weatherapp;

import weatherapp.Locality;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class WeatherAppModel
{
	
	ArrayList<Locality> places = new ArrayList<Locality>();
	Map<Integer, String> cache = new HashMap<Integer, String>();
	String[] activeForecast = new String[24];
	
	int cacheTime = 60;
	
	public int load(String path)
	{
		
		int count = 0;
		
		try
		{
			File fXmlFile = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
					
			NodeList nList = doc.getElementsByTagName("locality");

			for (int temp = 0; temp < nList.getLength(); temp++)
			{
				
				String name;
				float latitude = 0, longitude = 0;
				int altitude = 0;

				Node nNode = nList.item(temp);
						
				if (nNode.getNodeType() == Node.ELEMENT_NODE)
				{

					Element eElement = (Element) nNode;
					name = eElement.getAttribute("name");
					
					if (eElement.hasChildNodes())
					{
						NodeList nLocList = nNode.getChildNodes();
						Node nLocation = null;
						
						for (int i = 0; i < nLocList.getLength(); i++)
						{
							Node n = nLocList.item(i);
							
							if (n.getNodeType() == Node.ELEMENT_NODE)
							{
								nLocation = n;
								break;
							}
						}
						
						if (nLocation != null) // FUCKK
						{
							NamedNodeMap nLocAttrib = nLocation.getAttributes();
							
							try
							{
								latitude = Float.parseFloat(nLocAttrib.getNamedItem("latitude").getNodeValue());
								longitude = Float.parseFloat(nLocAttrib.getNamedItem("longitude").getNodeValue());
								altitude = Integer.parseInt(nLocAttrib.getNamedItem("altitude").getNodeValue());
							}
							catch (NumberFormatException e)
							{
								System.out.println("Error parsing places.xml\n" + e.getMessage());
							}
						}					
						
						Locality loc = new Locality(name, latitude, longitude, altitude);
						places.add(loc);
						count++;
						
					}
				}
			}
		}
		catch (Exception e)
		{
			return -1;
		}
		
		return count;
	}
	
	public void addLocation(String name, float latitude, float longitude, int altitude)
	{
		places.add(new Locality(name, latitude, longitude, altitude));
	}
	
	public void deleteLocation(int index)
	{
		places.remove(index);
	}
	
	public void updateLocation(int index, String name, float latitude, float longitude, int altitude)
	{
		Locality loc = places.get(index);
		
		loc.name = name;
		loc.latitude = latitude;
		loc.longitude = longitude;
		loc.altitude = altitude;
		loc.cacheTimer = 0;
	}
	
	public void addToTable(JTable jTable)
	{
	    for (Locality loc : places)
	    {
	        ((DefaultTableModel) jTable.getModel()).addRow(new Object[] 
       		{
	        	loc.name,
	        	loc.latitude,
	        	loc.longitude,
	        	loc.altitude
       		});
	    }
	}
	
	
	
	public int setCacheTimeSeconds() {
		return cacheTime;
	}

	public void setCacheTimeSeconds(int cacheTime) {
		this.cacheTime = cacheTime;
	}
	
	public static String fetchWeatherData(float latitude, float longitude, int altitude) throws Exception
	{
		
		String url = String.format(Locale.ROOT, "https://api.met.no/weatherapi/locationforecast/1.9/?lat=%.2f&lon=%.2f&msl=%o", latitude, longitude, altitude);
		
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", "");

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		System.out.println(response.toString());
		
		return response.toString();
	}

	public static String fetchWeatherData(Locality loc) throws Exception
	{
		return fetchWeatherData(loc.latitude, loc.longitude, loc.altitude);
	}
	
	public void fetchWeatherData(int index) throws Exception
	{
		Locality loc = places.get(index);
		if (loc.cacheTimer < System.currentTimeMillis())
		{
			System.out.println("Fetching data for index " + index + " from remote host...");
			loc.cacheTimer = System.currentTimeMillis() + cacheTime * 1000;
			String data = fetchWeatherData(loc);
			cache.put(index, data);
			parseResponse(data);
		}
		else
		{
			System.out.println("Fetching data for index " + index + " from cache...");
			parseResponse(cache.get(index));
		}
	}
	
	private static Document convertStringToXMLDocument(String xmlString)
    {
        //Parser that produces DOM object trees from XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         
        //API to obtain DOM Document instance
        DocumentBuilder builder = null;
        try
        {
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();
             
            //Parse the content to Document object
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            return doc;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
	
	public void parseResponse(String xml)
	{
		System.out.println("Parsing...");
		Document doc = convertStringToXMLDocument(xml);
		doc.getDocumentElement().normalize();
				
		NodeList nList = doc.getElementsByTagName("time");
		
		Element timeElement;
		Node timeNode;
		String from;
		String to;
		int count = 0;
		
		for (int i = 0; i < nList.getLength(); i++)
		{
			timeNode = nList.item(i);
			
			if (timeNode.getNodeType() == Node.ELEMENT_NODE)
			{

				timeElement = (Element) timeNode;
				from = timeElement.getAttribute("from");
				to = timeElement.getAttribute("to");
				
				if (from.equals(to))
				{
					activeForecast[count] = parseForecastNode(timeNode);
					count++;
				}
			}
			
			if (count == 24)
				break;
		}

	}
	
	public String parseForecastNode(Node node)
	{
		StringBuilder ss = new StringBuilder();
		
		Node locNode = getFirstChildElement(node);
		
		if (locNode != null)
		{
			ArrayList<Node> weatherNodes = getChildElements(locNode);
			
			for (Node weatherNode : weatherNodes)
			{
				ss.append(weatherNode.getNodeName().toUpperCase() + ": " + joinAttributes(weatherNode));
				ss.append(System.lineSeparator());
			}
		}
		
		return ss.toString();
	}
	
	public Node getFirstChildElement(Node node)
	{
		NodeList nodes = node.getChildNodes();
		
		for (int i = 0; i < nodes.getLength(); i++)
		{
			if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE)
				return nodes.item(i);
		}
		
		return null;
	}
	
	public ArrayList<Node> getChildElements(Node node)
	{
		ArrayList<Node> nodes = new ArrayList<Node>();
		
		NodeList children = node.getChildNodes();
		
		for (int i = 0; i < children.getLength(); i++)
		{
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
				nodes.add(children.item(i));
		}
		
		return nodes;
	}
	
	public String joinAttributes(Node node)
	{
		StringBuilder ss = new StringBuilder();
		
		NamedNodeMap nMap = node.getAttributes();
		
		Node mapNode;
		for (int i = 0; i < nMap.getLength(); i++)
		{
			mapNode = nMap.item(i);
			
			if (mapNode.getNodeName().equals("id"))
				continue;
			
			ss.append(mapNode.getNodeValue());
			
			if (i < nMap.getLength() - 1)
				ss.append(", ");
		}
		
		return ss.toString();
	}
	
	public String getForecast(int hour)
	{
		return activeForecast[hour];
	}
}
