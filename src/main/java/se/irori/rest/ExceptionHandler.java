package se.irori.rest;


import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
public class ExceptionHandler implements ExceptionMapper<Exception> {


  @Override
  public Response toResponse(Exception exception) {
    log.error("Something went wrong", exception);
    return Response.status(Status.INTERNAL_SERVER_ERROR)
        .build();
  }
}
