package com.atguigu.gmallmy0311.payment;

import com.atguigu.gmallmy0311.config.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPaymentApplicationTests {

	@Autowired
	private ActiveMQUtil activeMQUtil;

	@Test
	public void contextLoads() {

	}

	@Test
	public void testA() throws JMSException {
		Connection connection = activeMQUtil.getConnection();
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

		Queue atguigu = session.createQueue("test-gmall");
		MessageProducer producer = session.createProducer(atguigu);
		ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
		activeMQTextMessage.setText("困了,累了喝红牛！");

		producer.send(activeMQTextMessage);

		// 必须提交
		// session.commit();

		producer.close();
		session.close();
		connection.close();
	}


}
