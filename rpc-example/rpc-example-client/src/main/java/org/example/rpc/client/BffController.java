package org.example.rpc.client;

import org.example.rpc.api.User;
import org.example.rpc.client.exception.UserNotFoundException;
import org.example.rpc.core.common.exception.BusinessException;
import org.example.rpc.core.model.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Bff controller.
 */
@RequestMapping("/test")
@RestController
public class BffController {

  @Autowired
  private BffService bffService;

  /**
   * Test user.
   *
   * @return user
   */
  @GetMapping("/{id}")
  public CompletableFuture<ResponseEntity<?>> testUser(@PathVariable String id) {
    return bffService.getUserInfo(id)
        .handle((user, ex) -> {
          if (ex != null) {
            Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
            if (cause instanceof BusinessException) {
              BusinessException be = (BusinessException) cause;
              return ResponseEntity.status(be.getHttpStatus())
                  .body(new ErrorResponse(be.getErrorCode(), be.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "An error occurred"));
          }
          return ResponseEntity.ok().body(user);
        });
  }

  @PostMapping
  public CompletableFuture<User> createUser(@RequestBody User user) {
    return bffService.createUser(user);
  }

  @PutMapping("/{id}")
  public CompletableFuture<User> updateUser(@PathVariable String id, @RequestBody User user) {
    return bffService.updateUser(id, user);
  }

  @DeleteMapping("/{id}")
  public CompletableFuture<Void> deleteUser(@PathVariable String id) {
    return bffService.deleteUser(id);
  }

  @PostMapping("/batch")
  public CompletableFuture<List<User>> createUsers(@RequestBody List<User> users) {
    return bffService.createUsers(users);
  }

  @ExceptionHandler(UserNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
  }

}
