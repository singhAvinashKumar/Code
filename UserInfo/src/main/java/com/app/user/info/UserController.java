package com.app.user.info;

import com.app.user.model.ChangePassword;
import com.app.user.model.ResponseBody.Body;
import com.app.user.model.ResponseBody.CustomResponseBody;
import com.app.user.model.ResponseBody.Status;
import com.app.user.model.UserInfo;
import com.app.user.model.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@RestController
public class UserController {
    Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    @Autowired
    private Validator validator;

    //Get All User Info
    @RequestMapping("/")
    public ResponseEntity<CustomResponseBody> getAllUser(){
        logger.info("getAllUser Invoked");
        UUID uuid = UUID.randomUUID();
        Iterable<UserInfo> allUser = userService.getAllUser();
        return ResponseEntity.ok().body(new CustomResponseBody(uuid,new Date(),
                uuid, new Body(allUser),
                new Status(0, "",HttpStatus.OK),
                "getAllUser" ));

    }

    //Get User with given userId
    @RequestMapping("/{userId}")
    public Optional<UserInfo> getUser(@PathVariable int userId){
        Optional<UserInfo> temp=userService.getUser(userId);
        if (temp.isEmpty()){
            throw new UserNotFoundException("User ID"+userId);
        }
        return temp;
    }


    //Create User
    @RequestMapping(method = RequestMethod.POST,value = "/")
    public Object createUser(@RequestBody UserInfo info ){
        UUID uuid = UUID.randomUUID();
        logger.info("createUser Invoked ",info);

        if (userService.getUserByEmail(info.getEmail()).isPresent())
            {
                logger.info("getUserByEmail");

               return ResponseEntity.ok().body(new CustomResponseBody(uuid,new Date(),
                       uuid,
                       new Status(2154, "User already registered",HttpStatus.FOUND),
                       "createUser" ));
            }

       if (!validator.isValidEmail(info.getEmail()))
           {
               logger.info("Invalid EMAIL ID");
                  return ResponseEntity.ok().body(new CustomResponseBody(uuid,new Date(),
                   uuid,
                   new Status(2150, "Invalid User Email",HttpStatus.NOT_ACCEPTABLE),
                   "createUser" ));

           }
        logger.info("Email Validated");

       if (!validator.isValidPassword(info.getPassword()))
           {
               logger.info("isValidPassword");
               return  ResponseEntity.ok().body(new CustomResponseBody(uuid,new Date(),
                   uuid,
                   new Status(2150, "Invalid Password",HttpStatus.NOT_ACCEPTABLE),
                   "createUser" ));
           }
        logger.info("Password Validated");

        userService.createUser(info);
        return  ResponseEntity.ok().body(new CustomResponseBody(uuid,new Date(),
                uuid,
                new Status(0, "User Created",HttpStatus.OK),
                "createUser" ));
    }

    //Update User takes UserID
    @RequestMapping(method = RequestMethod.PUT,value = "/{userId}")
    public String updateUser (@RequestBody UserInfo info,@PathVariable int userId){
        info.setUserId(userId);
        userService.updateUser(info);
        return "updated "+ userId;
    }


    //changePassword
    @RequestMapping(method = RequestMethod.PUT,value = "/{userId}/change-password")
    public ResponseEntity<CustomResponseBody> changePassword (@RequestBody ChangePassword form, @PathVariable int userId){
            Optional<UserInfo> info = userService.getUser(userId);
        Status status = userService.changePassword(form, userId);
        UUID uuid = UUID.randomUUID();
        return ResponseEntity.ok().body(new CustomResponseBody(uuid,new Date(),uuid,status,"changePassword"));

    }


    //Delete User takes UserId
    @RequestMapping(method = RequestMethod.DELETE,value = "/{userId}")
    public ResponseEntity<CustomResponseBody> deleteUser(@PathVariable int userId){
        UUID uuid = UUID.randomUUID();
        var lambdaContext = new Object() {
            ResponseEntity<CustomResponseBody> response = null;
        };
        userService.getUser(userId).ifPresentOrElse(userInfo -> {
                    userService.deleteUser(userId);
                    lambdaContext.response = ResponseEntity.ok().body(new CustomResponseBody(uuid, new Date(),
                            uuid,
                            new Status(0, "User Deleted", HttpStatus.OK),
                            "deleteUser"));
                },()->
        {        lambdaContext.response = ResponseEntity.ok().body(new CustomResponseBody(uuid, new Date(),
                        uuid,
                        new Status(404, "User not found", HttpStatus.NOT_FOUND),
                        "deleteUser"));
        });
        return lambdaContext.response;
    }
}
