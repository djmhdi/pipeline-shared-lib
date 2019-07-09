package com.capgemini.fs.jenkins.plugins.spock

/**
 * Created by eggo1060 on 04/10/2017.
 * An exception class to exit a stage due to the when statement
 */
class WhenExitException extends Exception {

    public WhenExitException(String message)
    {
        super(message);
    }
}