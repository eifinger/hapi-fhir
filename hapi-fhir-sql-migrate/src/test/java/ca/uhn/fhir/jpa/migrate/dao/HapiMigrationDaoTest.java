package ca.uhn.fhir.jpa.migrate.dao;

import ca.uhn.fhir.jpa.migrate.config.TestMigrationConfig;
import ca.uhn.fhir.jpa.migrate.entity.HapiMigrationEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestMigrationConfig.class})
class HapiMigrationDaoTest {
	@Autowired
	HapiMigrationDao myHapiMigrationDao;

	@AfterEach
	void after() {
		myHapiMigrationDao.deleteAll();
	}

	@Test
	public void findAll_empty_returnsNothing() {
		List<HapiMigrationEntity> result = myHapiMigrationDao.findAll();
		assertThat(result, hasSize(0));
	}

	@Test
	public void findAll_2records_returnsBoth() {
		HapiMigrationEntity record1 = new HapiMigrationEntity();
		String desc1 = "DESC1";
		record1.setDescription(desc1);
		HapiMigrationEntity result1 = myHapiMigrationDao.save(record1);
		assertEquals(1, result1.getId());

		HapiMigrationEntity record2 = new HapiMigrationEntity();
		String desc2 = "DESC2";
		record2.setDescription(desc2);
		HapiMigrationEntity result2 = myHapiMigrationDao.save(record2);
		assertEquals(2, result2.getId());

		List<HapiMigrationEntity> all = myHapiMigrationDao.findAll();
		assertThat(all, hasSize(2));
	}
}
