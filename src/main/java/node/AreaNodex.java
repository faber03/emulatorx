package node;

import link.Link;
import link.Links;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public abstract class AreaNodex {

	private String areaName;
	private ClientConsumer consumer;
	private ClientSession sessionOut;
	private ClientProducer producer;
	private MessageHandler handler;
	private boolean multipleNorthboundQueues;
	private String urlIn;
	private String urlOut;
	protected Links links;
	private static final Logger log = LoggerFactory.getLogger(AreaNodex.class);
	protected DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

	public AreaNodex(String urlIn, String urlOut, String areaName, boolean multipleQueues) {
		this.areaName = areaName;
		this.urlIn = urlIn;
		this.urlOut = urlOut;
		this.links = new Links();
		multipleNorthboundQueues = multipleQueues;
		createConsumer();
		createProducer();
		setHandler(createMessageHandler());
		setQueueListener();
	}

	public void addLink(Link link) {
		this.links.addLink(link);
	}

	private void setHandler(MessageHandler handler) {
		this.handler = handler;
	}

	private void createConsumer() {
		ClientSessionFactory factoryIn;
		try {
			ServerLocator locatorIn = ActiveMQClient.createServerLocator(urlIn);
			factoryIn = locatorIn.createSessionFactory();
			ClientSession sessionIn = factoryIn.createSession(true, true);
			sessionIn.createQueue(new SimpleString(areaName), RoutingType.ANYCAST, new SimpleString(areaName), true);
			consumer = sessionIn.createConsumer(areaName);
			sessionIn.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createProducer(){
		ClientSessionFactory factoryOut;
		try{
			ServerLocator locatorOut = ActiveMQClient.createServerLocator(urlOut);
			factoryOut = locatorOut.createSessionFactory();
			sessionOut = factoryOut.createSession(true,true);
			sessionOut.start();

			if(multipleNorthboundQueues) {
				String NORTHBOUND_SUFFIX = "-Northbound";
				sessionOut.createQueue(new SimpleString(areaName + NORTHBOUND_SUFFIX), RoutingType.ANYCAST, new SimpleString(areaName + NORTHBOUND_SUFFIX), true);
				producer = sessionOut.createProducer(new SimpleString(areaName + NORTHBOUND_SUFFIX));
			} else {
				sessionOut.createQueue(new SimpleString("Northbound"), RoutingType.ANYCAST, new SimpleString("Northbound"), true);
				producer = sessionOut.createProducer(new SimpleString("Northbound"));
			}
			AreaNodexSender sender = new AreaNodexSender(sessionOut, producer,links);
			ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
			executorService.scheduleAtFixedRate(sender,0,3, TimeUnit.MINUTES);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setQueueListener() {
		try {
			consumer.setMessageHandler(handler);
		} catch (ActiveMQException e) {
			e.printStackTrace();
		}
	}

	protected abstract MessageHandler createMessageHandler() ;
}