PassCracker
============

This program is command-line tool to recover (brute-force) passwords for archives. It uses multiple threads to speed up the process, and may save current progress so you can continue the recovering process later.

Remember that you can easily recover forgotten passwords no longer than 5 characters, or passwords composed of several notable words (for ex. "anna1986" or "losangeles99pc", when you know possible words (i.e. "tokens") that may be in the forgotten password, and you know the minimum and maximum possible number of such words). Recovering of more complex passwords may take too much time.

Usage:
------

    java -jar PassCrackerCLI.jar [options...] [path to file]
  
    where options are:
    
      --minLength=[value] -- minimum amount of tokens in the generated passwords
      --maxLength=[value] -- maximum amount of tokens in the generated passwords
      --sequenceType=[value] -- type of sequence
      --alphabetType=[value] -- type of alphabet
      --threads=[value] (optional) -- number of working threads (by default: number of CPUs + 1)
      --saveProgress=[value] (optional) -- path to file for saving last used password
          (i.e. the progress) (the file will be owerwritten)
      --saveProgressTime=[value] (optional) -- amount of seconds between savings
      --log=[value] (optional) -- path to log file (only progress will be saved, runtime errors
          and result will not)
  
    Supported types of sequences and its options:
    
      type "simple" (bijective base-k numeration):
      
        --startFrom=[value] -- value to start sequence from (it may be the file from
            the "saveProgress" option) (JSON array of alphabet indices, or path to text file
            with JSON array of alphabet indices)
        
      type "permutations" (permutations without repetition):

        --startFrom=[value] -- value to start sequence from (it may be the file from
            the "saveProgress" option) (JSON array of alphabet indices, or path to text file
            with JSON array of alphabet indices)
    
    Supported types of alphabets and its options:
    
      type "characters":
      
        --characterSets=[value] -- sets of characters to use, as a sum of some of the next values:
            1 (000001) -- digits,
            2 (000010) -- latin characters,
            4 (000100) -- cyrillic characters,
            8 (001000) -- special characters,
            16 (010000) -- space,
            32 (100000) -- tab;
          for example, 19 (1 + 2 + 16) means "use digits and latin characters and space"
        --additionalCharacters=[value] -- additional characters to use
        
      type "tokens":
      
        --tokens=[value] -- path to text file with tokens (each token on new line);
          IMPORTANT:
            file must be saved in UTF-8 charset;
            beware the use of large file because the file will be completely loaded into memory
        --t[n]=[value] where n = 1,2,3,... -- additional tokents to include in the alphabet;
          for example: --t1=foo --t2=bar --t3=baz
    
    Current progress will be printed to STDERR.
    
    Result (i.e. the password) will be printed to STDUOT, so you may use something like this:
      java -jar PassCrackerCLI.jar <options> <path_to_file> > password.txt

Supported file formats:
-----------------------

* RAR (currently only on Linux or other Unix with unrar utility in the PATH)

TODO:
-----

* Add native supprot of RAR (use native libs through JNI)
* Add support of other archive formats
* Use more sophisticated way to describe set of possible passwords (something like regular expressions)

Requirements:
-------------

* JRE 1.8 or later
* Linux or other Unix
* unrar utility in the PATH
