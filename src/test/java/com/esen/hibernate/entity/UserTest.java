package com.esen.hibernate.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import junit.framework.Assert;

import org.hibernate.LazyInitializationException;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;
import org.hibernate.stat.SessionStatistics;
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
		//load总是返回一个带有id信息的代理对象
		assertNotNull(s);
	}

	@Test
	public void testGet() {
		User s = (User) HibernateUtil.get(User.class, "InvalidKey");
		assertNull(s);
	}

	@Test
	public void testGetAndLoad() {
		Session session = HibernateUtil.getCurrentSession();
		SessionStatistics stats = session.getStatistics();
		session.beginTransaction();
		User user = generateUser();
		session.save(user);
		//执行save方法后user被加入session的一级缓存
		Assert.assertEquals(1, stats.getEntityCount());
		session.flush();
		User s1 = (User) session.load(User.class, user.getId());
		User s2 = (User) session.get(User.class, user.getId());
		//load和get方法都是直接从一级缓存中获取数据所以是同一个实例
		Assert.assertTrue(s1 == s2);
		session.getTransaction().commit();
	}

	@Test(expected = StaleObjectStateException.class)
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
		//执行此处时，由于版本不一致抛出异常
		tr2.commit();
		s2.close();
	}

	private static User generateUser() {
		User user = new User();
		user.setName("sam001");
		return user;
	}
}
