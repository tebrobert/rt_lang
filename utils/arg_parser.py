import argparse


class ArgParser:
    def __init__(self):
        self.argParser = argparse.ArgumentParser()

    def add(self, x, **kwargs):
        new_parser = ArgParser()
        new_parser.argParser = self.argParser
        new_parser.argParser.add_argument(x, **kwargs)
        return new_parser

    def parse(self, arg_line):
        return self.argParser.parse_args(arg_line)
