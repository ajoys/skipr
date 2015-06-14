import random

def getRoomName():
    roomAdjective = getRandomWord('adjectives.txt')
    animalName = getRandomWord('animals.txt')
    roomName = roomAdjective + ' ' + animalName
    roomName = roomName.upper()
    return roomName

def getRandomWord(file):
    wordFile = open('nameGenerator/' + file, 'r')
    stringOfWords = wordFile.read().lower()
    wordFile.close()

    # removing all new lines
    stringOfWords = stringOfWords.replace('\n', '')

    # get a list of adjectives
    listOfWords = list(stringOfWords.split(','))
    totalWords = len(listOfWords)

    # generate a random number and return a random word
    randomNumber = random.randint(0, totalWords - 1)
    return listOfWords[randomNumber]