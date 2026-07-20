package com.dhc.inspection_system.dao;

import java.util.Optional;

public interface DownloadDAO {

    Optional<String> findFileNameByUniqueId(String uniqueId);

}
