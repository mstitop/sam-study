package com.esen.hibernate.entity;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.stat.Statistics;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 测试Hibernate的get、load访问与缓存的关系
 * @author sam
 *
 */
public class CacheTest {
	private static SessionFactory factory;

	@BeforeClass
	public static void beforeClass() {
		Configuration cfg = new Configuration().configure();
		cfg.addAnnotatedClass(User.class);
		factory = cfg.buildSessionFactory();
	}

	@After
	public void after() {
		factory.getCache().evictEntityRegion(User.class);
		factory.getStatistics().clear();
	}

	@Test
	public void testSessionGet() {
		User user = generateUser();
		assertGetMissCache(user.getId());
		assertGetHitCache(user.getId());
		
		factory.getCache().evictEntityRegion(User.class);
		assertGetMissCache(user.getId());
	}

	/**
	 * 调用load方法的时候,hibernate一开始并没有查询二级缓存或是数据库, 而是先返回一个代理对象,
	 * 该对象只包含id,只有显示调用对象的非id属性时,比如user.getName(),hibernate才会去二级缓存查找,
	 * 如果没命中缓存再去数据库找,数据库还找不到则抛异常.load方法会尽量推迟对象的查找工作,这是它跟get方法最大的区别. 
	 */
	@Test
	public void testSessionLoad() {
		User user = generateUser();
		assertLoadMissCache(user.getId());
		assertLoadHitCache(user.getId());
	}

	private User generateUser() {
		User user = new User();
		user.setName("this is a name");
		Session session = factory.openSession();
		session.beginTransaction();
		session.save(user);
		session.getTransaction().commit();
		session.close();
		factory.getCache().evictEntityRegion(User.class);
		return user;
	}

	private void assertGetMissCache(Serializable id) {
		Statistics stats = factory.getStatistics();
		long count = factory.getStatistics().getSecondLevelCacheMissCount();
		Session session = factory.openSession();
		session.beginTransaction();
		session.get(User.class, id);
		session.getTransaction().commit();
		session.close();
		Assert.assertEquals(count + 1, stats.getSecondLevelCacheMissCount());
	}

	private void assertGetHitCache(Serializable id) {
		Statistics statistics = factory.getStatistics();
		long count = statistics.getSecondLevelCacheHitCount();
		Session session = factory.openSession();
		session.beginTransaction();
		session.get(User.class, id);
		session.getTransaction().commit();
		session.close();
		Assert.assertEquals(count + 1, statistics.getSecondLevelCacheHitCount());
	}

	private void assertLoadMissCache(Serializable id) {
		Statistics stats = factory.getStatistics();
		long count = factory.getStatistics().getSecondLevelCacheMissCount();
		Session session = factory.openSession();
		session.beginTransaction();
		User u = (User) session.load(User.class, id);
		//注意如果不访问非ID时，u只是一个代理对象，不会访问二级缓存
		u.getName();
		session.getTransaction().commit();
		session.close();
		Assert.assertEquals(count + 1, stats.getSecondLevelCacheMissCount());
	}

	private void assertLoadHitCache(Serializable id) {
		Statistics statistics = factory.getStatistics();
		long count = statistics.getSecondLevelCacheHitCount();
		Session session = factory.openSession();
		session.beginTransaction();
		User u = (User) session.load(User.class, id);
		//注意如果不访问非ID时，u只是一个代理对象，不会访问二级缓存
		u.getName();
		session.getTransaction().commit();
		session.close();
		Assert.assertEquals(count + 1, statistics.getSecondLevelCacheHitCount());
	}
}
