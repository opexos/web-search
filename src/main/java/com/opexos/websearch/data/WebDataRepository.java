package com.opexos.websearch.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WebDataRepository extends JpaRepository<WebData, Long> {

    //super slow inefficient text search
    @Query("select wd.url from WebData wd where lower(wd.pageBody) like concat('%',lower(?1),'%')")
    List<String> findUrlsByPageBodyContains(String text);

}
