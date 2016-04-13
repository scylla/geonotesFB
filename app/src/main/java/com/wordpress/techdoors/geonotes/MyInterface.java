package com.wordpress.techdoors.geonotes;

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;

/**
 * Created by amit on 22/03/16.
 */
public interface MyInterface {

        /**
         * Invoke the Lambda function "AndroidBackendLambdaFunction".
         * The function name is the method name.
         */
        @LambdaFunction
        ResponseClass AndroidBackendLambdaFunction(RequestClass request);


}
