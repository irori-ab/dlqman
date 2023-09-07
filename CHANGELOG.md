# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [unreleased]
### Added
- N/A

### Changed
- N/A 

## [1.0.0] - 2023-08-30
### Added
Initial Release with limited functionality:
Strategies:
- `void` - can be used to drop the message without persisting it
- `dismiss` - can be used to skip the message, but adding it to the persistance module
- `SimpleResendDLQStrategy` can be used to resend messages instantly or after a time interval

Matchers:
- `all` - matches all messages
- `HeaderRegexMatcher` - can be used to match headers by using regular expressions

[Unreleased]: https://github.com/irori-ab/dlqman/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/irori-ab/v/releases/tag/v1.0.0

