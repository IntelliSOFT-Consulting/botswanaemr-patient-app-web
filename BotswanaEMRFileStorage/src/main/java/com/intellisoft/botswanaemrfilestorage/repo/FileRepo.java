package com.intellisoft.botswanaemrfilestorage.repo;

import com.intellisoft.botswanaemrfilestorage.model.Files;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepo extends JpaRepository<Files, String> {
}