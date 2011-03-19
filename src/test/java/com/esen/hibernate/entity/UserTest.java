package com.esen.hibernate.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.hibernate.LazyInitializationException;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esen.hibernate.util.HibernateUtil;

public class UserTest {
	private static Logger log = LoggerFactory.getLogger(UserTest.class);

	private static String key;

	@BeforeClass
	public static void create() {
		User user = generateUser();
		HibernateUtil.save(user);
		key = user.getId();
	}

	@Test
	public void testAddUser() {
		User user = generateUser();
		HibernateUtil.save(user);
		User s = (User) HibernateUtil.get(User.class, user.getId());
		assertEquals(user.getId(), s.getId());
	}

	
	@Test(expected = LazyInitializationException.class)
	public void testLazyByLoad() {
		User user = generateUser();
		HibernateUtil.save(user);
		User s = (User) HibernateUtil.load(User.class, user.getId());
		s.getName();
	}

	@Test
	public void testLazyByGet() {
		User user = generateUser();
		HibernateUtil.save(user);
		User s = (User) HibernateUtil.get(User.class, user.getId());
		assertNotNull(s.getName());
	}

	@Test
	public void testLoad() {
		User s = (User) HibernateUtil.load(User.class, "InvalidKey");
		assertNotNull(s);
	}

	@Test
	public void testGet() {
		User s = (User) HibernateUtil.get(User.class, "InvalidKey");
		assertNull(s);
	}

	@Test(expected=StaleObjectStateException.class)
	public void testVersion() {
		Session s1 = HibernateUtil.getSessionFactory().openSession();
		Session s2 = HibernateUtil.getSessionFactory().openSession();
		
		User e1 = (User) s1.get(User.class, key);
		User e2 = (User) s2.get(User.class, key);
		
		Transaction tr1 = s1.beginTransaction();
		e1.setName("changed by java");
		tr1.commit();
		s1.close();
		
		Transaction tr2 = s2.beginTransaction();
		e2.setName("changed by e1");
		tr2.commit();
		s2.close();
	}

	private static User generateUser() {
		User user = new User();
		user.setName("sam");
		return user;
	}
}
