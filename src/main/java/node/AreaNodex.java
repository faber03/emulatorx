package node;

import link.Links;
import link.Link;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.format.DateTimeFormatter;


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
	protected int scala;
	private static final Logger log = LoggerFactory.getLogger(AreaNodex.class);
	protected DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

	public AreaNodex(String urlIn, String urlOut, String areaName, boolean multipleQueues, int scala) {
		this.areaName = areaName;
		this.urlIn = urlIn;
		this.urlOut = urlOut;
		this.links = new Links();
		multipleNorthboundQueues = multipleQueues;
		this.scala = scala;
		createConsumer();
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



	public void createProducer(){
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
			/*RTAggregateTravelTimeAreaNodeSender sender = new RTAggregateTravelTimeAreaNodeSender(sessionOut, producer,links);
			ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
			long period = 3/scala;
			executorService.scheduleAtFixedRate(sender,period,period,TimeUnit.MINUTES);*/
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

	public void sendMessage(String messageBody){
		ClientMessage msg;
		try {
			msg = sessionOut.createMessage(true);
			msg.getBodyBuffer().writeString(messageBody);
			producer.send(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract MessageHandler createMessageHandler() ;
}