package com.esen.hibernate.entity;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esen.hibernate.util.HibernateUtil;

public class UserTest {
	private static Logger log = LoggerFactory.getLogger(UserTest.class);

	@Test
	public void testAdd() {
		log.debug("test");
		User user = new User();
		user.setName("test");
		HibernateUtil.save(user);
		assertNotNull(HibernateUtil.load(User.class, user.getId()));
	}
}
