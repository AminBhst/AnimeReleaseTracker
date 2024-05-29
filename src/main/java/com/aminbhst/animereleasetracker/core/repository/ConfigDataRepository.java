package com.aminbhst.animereleasetracker.core.repository;

import com.aminbhst.animereleasetracker.core.model.ConfigData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

public interface ConfigDataRepository extends Repository<ConfigData, Long>, CrudRepository<ConfigData, Long> {

}
