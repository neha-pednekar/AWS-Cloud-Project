package com.csye6255.web.application.fall2018.dao;

import com.csye6255.web.application.fall2018.pojo.Attachment;
import com.csye6255.web.application.fall2018.pojo.Transaction;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface  AttachmentDAO extends CrudRepository<Attachment, Long> {

    List<Attachment> findByTransaction(Transaction transaction);
    List<Attachment> findAttachmentByAttachmentid(String attachmentId);
}
