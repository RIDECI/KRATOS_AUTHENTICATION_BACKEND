package edu.dosw.rideci.infrastructure.persistance.repository.mapper;
import java.util.List;

import org.mapstruct.Mapper;

import edu.dosw.rideci.domain.models.UserAuth;
import edu.dosw.rideci.infrastructure.controllers.dto.Response.UserResponse;
import edu.dosw.rideci.application.events.UserEvent;
import edu.dosw.rideci.infrastructure.persistance.entity.UserAuthDocument;

@Mapper(componentModel = "spring")
public interface UserAuthMapper {

    UserAuthDocument toDocument(UserAuth userAuth);

    UserAuth toDomain(UserAuthDocument userAuthDocument);

    List<UserAuth> toDomainList(List<UserAuthDocument> userAuthDocuments);

    List<UserAuthDocument> toDocumentList(List<UserAuth> userAuthList);

    UserResponse toResponseDTO(UserAuth userAuth);

    UserEvent toUserAuthEvent(UserAuthDocument userAuthDocument);
}