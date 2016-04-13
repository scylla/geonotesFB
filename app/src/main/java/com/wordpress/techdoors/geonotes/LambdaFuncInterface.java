package com.wordpress.techdoors.geonotes;

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;

/**
 * Created by amit on 02/04/16.
 */
public interface LambdaFuncInterface {

    @LambdaFunction
    String androidTest(NameInfo nameInfo);

    @LambdaFunction
    String createTopic(GeoTopic topicName);
}
