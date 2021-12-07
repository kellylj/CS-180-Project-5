package packets.response;

import server.LearningManagementSystemServer;
import datastructures.Quiz;

/**
 * Gets the requested quiz to be returned to the client
 *
 * @author Liam Kelly
 *
 * @version October 7, 2021
 *
 **/

public class QuizResponsePacket extends ResponsePacket{
    Quiz quizToReturn;

    public QuizResponsePacket(LearningManagementSystemServer lms, int id) {
        quizToReturn = lms.getQuizManager().searchQuizByID(id);
    }

    /**
     * Allows the client to get the requested quiz
     *
     * @return quizToReturn - the requested quiz
     *
     */

    public Quiz getQuizResponse() {
        return quizToReturn;
    }

}